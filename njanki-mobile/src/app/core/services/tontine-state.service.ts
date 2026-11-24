import { Injectable, signal, computed, inject, DestroyRef, effect, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Preferences } from '@capacitor/preferences';
import { Observable, forkJoin, from, of } from 'rxjs';
import { catchError, concatMap, map, switchMap } from 'rxjs/operators';
import {
  Groupe,
  SessionTontine,
  MandatBureau,
  TypeSiege,
  FrequenceReunion,
  StatutGroupe,
} from '@shared/models/groupe.model';
import { Membre, StatutMembre } from '@shared/models/membre.model';
import { Cotisation, TourPot, TypeCotisation, StatutCotisation } from '@shared/models/cotisation.model';
import { Paiement, ModePaiement, StatutPaiement } from '@shared/models/paiement.model';
import { Reunion, PresenceMembre, StatutPresence, StatutReunion } from '@shared/models/reunion.model';
import { Penalite, TarificationPenalite, TypeInfraction, StatutPenalite } from '@shared/models/penalite.model';
import { ApiResponse } from '@shared/models/api-response.model';
import { NetworkService } from './network.service';
import { MobileApiService } from './api.service';
import { MobileAuthService } from './auth.service';

export const OFFLINE_QUEUE_KEY = 'njangi_offline_queue';
export const CACHED_STATE_KEY = 'njangi_cached_state';

export type OfflineActionType =
  | 'PRESENCE'
  | 'CASH_PAYMENT'
  | 'MOMO_PAYMENT'
  | 'SANCTION'
  | 'CREATE_GROUP';

export interface OfflineAction {
  id: string;
  type: OfflineActionType;
  payload: Record<string, unknown>;
  timestamp: string;
  retryCount?: number;
}

export interface CreerGroupeMobileInput {
  nom: string;
  description: string;
  typeSiege: TypeSiege;
  adresseSiegeFixe?: string;
  frequenceReunion: FrequenceReunion;
  montantCotisationBaseXaf: number;
  nombreMembresMax: number;
  reglementInterieur?: string;
}

interface CachedTontineState {
  groupes: Groupe[];
  membres: Membre[];
  cotisations: Cotisation[];
  paiements: Paiement[];
  sanctions: Penalite[];
  presences: PresenceMembre[];
  cachedAt: string;
}

interface RawGroupeDto {
  id: string;
  nom: string;
  description?: string;
  createurMembreId?: string;
  typeSiege?: TypeSiege;
  adresseSiege?: string;
  adresseSiegeFixe?: string;
  frequenceReunion?: FrequenceReunion;
  montantCotisationPrincipale?: number;
  montantCotisationBaseXaf?: number;
  nombreMembresMax?: number;
  nombreMembresActuels?: number;
  statut?: StatutGroupe;
  premierBureauElu?: boolean;
  soldeCaisseCashXaf?: number;
  soldeCaisseMobileMoneyXaf?: number;
  createdAt?: string;
  dateCreation?: string;
}

interface RawMembreDto {
  id: string;
  authUtilisateurId?: string;
  nom: string;
  prenom: string;
  telephone?: string;
  email?: string;
  photoUrl?: string;
  avatarUrl?: string;
  ville?: string;
  pays?: string;
  statut?: StatutMembre;
  estVerifieKyc?: boolean;
  createdAt?: string;
  dateInscription?: string;
}

interface RawReunionDto {
  id: string;
  groupeId: string;
  sessionTontineId?: string;
  sessionId?: string;
  titre: string;
  dateReunion?: string;
  heureDebut?: string;
  typeSiege?: TypeSiege;
  hoteId?: string;
  hoteMembreId?: string;
  lieuReunion?: string;
  lieuAdresse?: string;
  ordreJour?: string;
  ordreDuJour?: string[];
  statut?: StatutReunion;
}

interface RawCotisationDto {
  id: string;
  groupeId: string;
  sessionId?: string;
  reunionId?: string;
  membreId: string;
  membreNom?: string;
  typeCotisation?: TypeCotisation;
  montantAttendu?: number;
  montantAttenduXaf?: number;
  montantPaye?: number;
  montantPayeXaf?: number;
  dateEcheance?: string;
  statut?: StatutCotisation;
}

interface RawPaiementDto {
  id: string;
  cotisationId?: string;
  groupeId: string;
  sessionId?: string;
  membreId: string;
  membreNom?: string;
  montant?: number;
  montantXaf?: number;
  modePaiement?: ModePaiement;
  typeCotisation?: TypeCotisation;
  statut?: StatutPaiement;
  datePaiement?: string;
  createdAt?: string;
  cleIdempotence?: string;
  pieceJointeUrl?: string;
  preuveUrl?: string;
  referenceTransaction?: string;
  valideParTresorierId?: string;
  commentaire?: string;
}

interface RawPenaliteDto {
  id: string;
  groupeId: string;
  sessionId?: string;
  reunionId?: string;
  membreId: string;
  membreNom?: string;
  typeInfraction?: TypeInfraction;
  montant?: number;
  montantXaf?: number;
  motif?: string;
  statut?: StatutPenalite;
  dateApplication?: string;
  createdAt?: string;
  dateReglement?: string;
  appliqueParNom?: string;
}

@Injectable({
  providedIn: 'root',
})
export class MobileTontineStateService {
  private readonly apiService = inject(MobileApiService);
  private readonly authService = inject(MobileAuthService);
  private readonly networkService = inject(NetworkService);
  private readonly destroyRef = inject(DestroyRef);

  // File d'attente d'actions hors-ligne persistée dans @capacitor/preferences (mode dégradé 3G)
  readonly offlineQueue = signal<OfflineAction[]>([]);
  readonly isSyncing = signal<boolean>(false);
  readonly lastSyncAt = signal<string | null>(null);
  readonly syncError = signal<string | null>(null);

  // Groupes
  readonly groupes = signal<Groupe[]>([
    {
      id: 'grp-001',
      nom: 'Tontine des Élites de Bafoussam - Yaoundé',
      description: 'Solidarité, épargne rotative et entraide mutuelle.',
      createurMembreId: 'm-002',
      typeSiege: 'FIXE',
      adresseSiegeFixe: 'Maison du Peuple, Quartier Bastos, Yaoundé',
      frequenceReunion: 'MENSUELLE',
      jourSeance: '1er Samedi du mois',
      reglementInterieurUrl: '/docs/reglement-bafoussam.pdf',
      montantCotisationBaseXaf: 50000,
      devise: 'XAF',
      nombreMembresMax: 15,
      nombreMembresActuels: 5,
      statut: 'ACTIF',
      premierBureauElu: true,
      soldeCaisseCashXaf: 50000,
      soldeCaisseMobileMoneyXaf: 0,
      dateCreation: '2024-01-15T10:00:00Z',
    },
  ]);

  readonly activeGroupId = signal<string>('grp-001');

  readonly activeGroupe = computed(() => {
    return this.groupes().find((g) => g.id === this.activeGroupId()) || this.groupes()[0];
  });

  // Distinction Mandat vs Session
  readonly activeMandat = signal<MandatBureau>({
    id: 'man-2025-2027',
    groupeId: 'grp-001',
    presidentMembreId: 'm-002',
    presidentNom: 'Maurice KAMDEM',
    tresorierMembreId: 'm-003',
    tresorierNom: 'Clarisse NGUEMO',
    secretaireMembreId: 'm-004',
    secretaireNom: 'Alain TCHOUA',
    commissaireAuxComptesId: 'm-005',
    dateDebut: '2025-01-01',
    dateFinPrevue: '2027-01-01',
    estActif: true,
  });

  readonly activeSession = signal<SessionTontine>({
    id: 'ses-2026',
    groupeId: 'grp-001',
    numeroSession: 1,
    libelle: 'Cycle Annuel 2026',
    dateDebut: '2026-01-01',
    dateFinPrevue: '2026-12-31',
    montantPotParTourXaf: 600000,
    montantSecoursXaf: 5000,
    montantReserveXaf: 10000,
    nombreToursPrevu: 12,
    tourActuel: 3,
    estActive: true,
    totalCollecteXaf: 1800000,
    totalDistribueXaf: 1200000,
  });

  // Membres
  readonly membres = signal<Membre[]>([
    {
      id: 'm-001',
      authUtilisateurId: 'u-001',
      nom: 'FOKOU',
      prenom: 'Jean-Paul',
      email: 'jp.fokou@njangi.cm',
      telephone: '+237699123456',
      statut: 'ACTIF',
      ville: 'Yaoundé',
      pays: 'Cameroun',
      dateInscription: '2024-01-15',
      createdAt: '2024-01-15T10:00:00Z',
      estVerifieKyc: true,
    },
    {
      id: 'm-002',
      authUtilisateurId: 'u-002',
      nom: 'KAMDEM',
      prenom: 'Maurice',
      email: 'maurice.kamdem@njangi.cm',
      telephone: '+237677234567',
      statut: 'ACTIF',
      ville: 'Yaoundé',
      pays: 'Cameroun',
      dateInscription: '2024-01-15',
      createdAt: '2024-01-15T10:00:00Z',
      estVerifieKyc: true,
    },
    {
      id: 'm-003',
      authUtilisateurId: 'u-003',
      nom: 'NGUEMO',
      prenom: 'Clarisse',
      email: 'clarisse.nguemo@njangi.cm',
      telephone: '+237690345678',
      statut: 'ACTIF',
      ville: 'Yaoundé',
      pays: 'Cameroun',
      dateInscription: '2024-01-15',
      createdAt: '2024-01-15T10:00:00Z',
      estVerifieKyc: true,
    },
    {
      id: 'm-004',
      authUtilisateurId: 'u-004',
      nom: 'TCHOUA',
      prenom: 'Alain',
      email: 'alain.tchoua@njangi.cm',
      telephone: '+237651456789',
      statut: 'ACTIF',
      ville: 'Yaoundé',
      pays: 'Cameroun',
      dateInscription: '2024-01-15',
      createdAt: '2024-01-15T10:00:00Z',
      estVerifieKyc: true,
    },
    {
      id: 'm-005',
      authUtilisateurId: 'u-005',
      nom: 'KENMOE',
      prenom: 'Béatrice',
      email: 'beatrice.kenmoe@njangi.cm',
      telephone: '+237699567890',
      statut: 'ACTIF',
      ville: 'Yaoundé',
      pays: 'Cameroun',
      dateInscription: '2024-01-15',
      createdAt: '2024-01-15T10:00:00Z',
      estVerifieKyc: true,
    },
  ]);

  // Tours du pot rotatif
  readonly toursPot = signal<TourPot[]>([
    {
      id: 'pot-tour-1',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      numeroTour: 1,
      beneficiaireMembreId: 'm-002',
      beneficiaireNom: 'Maurice KAMDEM',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 600000,
      dateAttribution: '2026-01-03',
      statut: 'VERSE',
      recuSigneUrl: '/receipts/pot-jan-2026.pdf',
    },
    {
      id: 'pot-tour-2',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      numeroTour: 2,
      beneficiaireMembreId: 'm-001',
      beneficiaireNom: 'Jean-Paul FOKOU',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 600000,
      dateAttribution: '2026-02-07',
      statut: 'VERSE',
      recuSigneUrl: '/receipts/pot-feb-2026.pdf',
    },
    {
      id: 'pot-tour-3',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      numeroTour: 3,
      beneficiaireMembreId: 'm-003',
      beneficiaireNom: 'Clarisse NGUEMO',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 600000,
      dateAttribution: '2026-03-07',
      statut: 'PRET_POUR_VERSEMENT',
    },
    {
      id: 'pot-tour-4',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      numeroTour: 4,
      beneficiaireMembreId: 'm-004',
      beneficiaireNom: 'Alain TCHOUA',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 0,
      dateAttribution: '2026-04-04',
      statut: 'A_VENIR',
    },
    {
      id: 'pot-tour-5',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      numeroTour: 5,
      beneficiaireMembreId: 'm-005',
      beneficiaireNom: 'Béatrice KENMOE',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 0,
      dateAttribution: '2026-05-02',
      statut: 'A_VENIR',
    },
  ]);

  // Présences
  readonly presences = signal<PresenceMembre[]>([
    { id: 'p-1', reunionId: 'reu-live-03', membreId: 'm-001', membreNom: 'Jean-Paul FOKOU', statut: 'PRESENT', heureArrivee: '13:50' },
    { id: 'p-2', reunionId: 'reu-live-03', membreId: 'm-002', membreNom: 'Maurice KAMDEM', statut: 'PRESENT', heureArrivee: '13:45' },
    { id: 'p-3', reunionId: 'reu-live-03', membreId: 'm-003', membreNom: 'Clarisse NGUEMO', statut: 'PRESENT', heureArrivee: '13:55' },
    { id: 'p-4', reunionId: 'reu-live-03', membreId: 'm-004', membreNom: 'Alain TCHOUA', statut: 'RETARD', heureArrivee: '14:25' },
    { id: 'p-5', reunionId: 'reu-live-03', membreId: 'm-005', membreNom: 'Béatrice KENMOE', statut: 'EXCUSE', motifAbsence: 'Mission professionnelle à Douala' },
  ]);

  // Réunion active (Séance live)
  readonly activeReunion = signal<Reunion>({
    id: 'reu-live-03',
    groupeId: 'grp-001',
    sessionId: 'ses-2026',
    numeroOrdre: 3,
    titre: 'Séance Mensuelle Ordinaire #03',
    dateReunion: '2026-03-07',
    heureDebut: '14:00',
    typeSiege: 'FIXE',
    lieuAdresse: 'Maison du Peuple, Yaoundé',
    statut: 'EN_COURS',
    presidentSeanceId: 'm-002',
    secretaireSeanceId: 'm-004',
    hoteMembreId: 'm-003',
    ordreDuJour: [
      '1. Accueil et installation des membres',
      '2. Appel nominatif et vérification du quorum',
      '3. Collecte des cotisations (Pot rotatif + Secours + Réserve)',
      '4. Attribution et décaissement du Pot du mois à Mme Clarisse Nguemo',
      '5. Traitement des cas sociaux et sanctions de séance',
      '6. Divers et clôture',
    ],
    presences: [],
    beneficiairePotDuJourId: 'm-003',
    totalCollecteSeanceXaf: 65000,
    totalAmendesSeanceXaf: 1000,
  });

  // Sanctions & Barème
  readonly bareme = signal<TarificationPenalite[]>([
    { id: 'tar-1', groupeId: 'grp-001', typeInfraction: 'RETARD_REUNION', libelle: 'Retard à la réunion (> 15 min)', montantForfaitaireXaf: 1000, estPourcentage: false, actif: true },
    { id: 'tar-2', groupeId: 'grp-001', typeInfraction: 'ABSENCE_NON_JUSTIFIEE', libelle: 'Absence non justifiée', montantForfaitaireXaf: 5000, estPourcentage: false, actif: true },
    { id: 'tar-3', groupeId: 'grp-001', typeInfraction: 'BAVARDAGE_SONNERIE', libelle: 'Sonnerie téléphone / Trouble de séance', montantForfaitaireXaf: 500, estPourcentage: false, actif: true },
    { id: 'tar-4', groupeId: 'grp-001', typeInfraction: 'RETARD_PAIEMENT', libelle: 'Retard versement cotisation', montantForfaitaireXaf: 2000, estPourcentage: false, actif: true },
  ]);

  readonly sanctions = signal<Penalite[]>([
    {
      id: 'sanc-001',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      reunionId: 'reu-live-03',
      membreId: 'm-004',
      membreNom: 'Alain TCHOUA',
      typeInfraction: 'RETARD_REUNION',
      montantXaf: 1000,
      motif: 'Arrivée à 14h25 au lieu de 14h00',
      statut: 'PAYEE',
      dateApplication: '2026-03-07T14:30:00Z',
      dateReglement: '2026-03-07T14:35:00Z',
      appliqueParNom: 'Maurice KAMDEM (Président)',
    },
  ]);

  // Cotisations et Paiements
  readonly cotisations = signal<Cotisation[]>([
    {
      id: 'cot-001',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      reunionId: 'reu-live-03',
      membreId: 'm-001',
      membreNom: 'Jean-Paul FOKOU',
      typeCotisation: 'ROTATIVE_POT',
      montantAttenduXaf: 50000,
      montantPayeXaf: 50000,
      statut: 'PAYE',
      dateEcheance: '2026-03-07',
    },
    {
      id: 'cot-002',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      reunionId: 'reu-live-03',
      membreId: 'm-001',
      membreNom: 'Jean-Paul FOKOU',
      typeCotisation: 'SECOURS_DECES',
      montantAttenduXaf: 5000,
      montantPayeXaf: 5000,
      statut: 'PAYE',
      dateEcheance: '2026-03-07',
    },
    {
      id: 'cot-003',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      reunionId: 'reu-live-03',
      membreId: 'm-001',
      membreNom: 'Jean-Paul FOKOU',
      typeCotisation: 'CAISSE_RESERVE',
      montantAttenduXaf: 10000,
      montantPayeXaf: 10000,
      statut: 'PAYE',
      dateEcheance: '2026-03-07',
    },
  ]);

  readonly paiements = signal<Paiement[]>([
    {
      id: 'pay-001',
      cotisationId: 'cot-001',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      membreId: 'm-001',
      membreNom: 'Jean-Paul FOKOU',
      montantXaf: 50000,
      modePaiement: 'CASH',
      typeCotisation: 'ROTATIVE_POT',
      statut: 'VALIDE',
      datePaiement: '2026-03-07T14:15:00Z',
      valideParTresorierId: 'm-003',
      preuveUrl: 'https://images.unsplash.com/photo-1554415707-9e4966a604f7?w=600&q=80',
      cleIdempotence: 'idemp-cash-001-20260307',
    },
  ]);

  // Trésorerie
  readonly totalCaissePhysiqueCash = computed(() => {
    return this.paiements()
      .filter((p) => p.modePaiement === 'CASH' && p.statut === 'VALIDE')
      .reduce((acc, curr) => acc + curr.montantXaf, 0);
  });

  readonly totalMobileMoney = computed(() => {
    return this.paiements()
      .filter(
        (p) =>
          (p.modePaiement === 'MTN_MOMO' || p.modePaiement === 'ORANGE_MONEY') &&
          p.statut === 'VALIDE'
      )
      .reduce((acc, curr) => acc + curr.montantXaf, 0);
  });

  readonly totalTresorerieGlobale = computed(() => {
    return this.totalCaissePhysiqueCash() + this.totalMobileMoney();
  });

  private previousOnlineState = this.networkService.isOnline();
  private hasLoadedSessionData = false;

  constructor() {
    this.restorePersistedState();

    effect(() => {
      const online = this.networkService.isOnline();
      const authenticated = this.authService.isAuthenticated();
      const transitionedToOnline = online && !this.previousOnlineState;
      this.previousOnlineState = online;

      if (!authenticated) {
        this.hasLoadedSessionData = false;
        return;
      }

      if (online && !this.hasLoadedSessionData) {
        this.hasLoadedSessionData = true;
        untracked(() => this.chargerDonneesReelles());
      }

      if (transitionedToOnline) {
        const shouldSync = untracked(
          () => this.offlineQueue().length > 0 && !this.isSyncing()
        );
        if (shouldSync) {
          untracked(() => this.synchroniserActionsHorsLigne());
        }
      }
    });
  }

  private restorePersistedState(): void {
    forkJoin([
      from(Preferences.get({ key: OFFLINE_QUEUE_KEY })).pipe(catchError(() => of({ value: null }))),
      from(Preferences.get({ key: CACHED_STATE_KEY })).pipe(catchError(() => of({ value: null }))),
    ])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ([queueRes, cacheRes]) => {
          if (queueRes.value) {
            try {
              const parsedQueue = JSON.parse(queueRes.value) as OfflineAction[];
              if (Array.isArray(parsedQueue)) {
                this.offlineQueue.set(parsedQueue);
              }
            } catch {
              // Ignorer une file corrompue
            }
          }

          if (cacheRes.value) {
            try {
              const cached = JSON.parse(cacheRes.value) as Partial<CachedTontineState>;
              if (Array.isArray(cached.groupes) && cached.groupes.length > 0) {
                this.groupes.set(cached.groupes);
                this.activeGroupId.set(cached.groupes[0].id);
              }
              if (Array.isArray(cached.membres) && cached.membres.length > 0) {
                this.membres.set(cached.membres);
              }
              if (Array.isArray(cached.cotisations)) {
                this.cotisations.set(cached.cotisations);
              }
              if (Array.isArray(cached.paiements)) {
                this.paiements.set(cached.paiements);
              }
              if (Array.isArray(cached.sanctions)) {
                this.sanctions.set(cached.sanctions);
              }
              if (Array.isArray(cached.presences)) {
                this.presences.set(cached.presences);
              }
              if (cached.cachedAt) {
                this.lastSyncAt.set(cached.cachedAt);
              }
            } catch {
              // Ignorer un cache corrompu
            }
          }
        },
      });
  }

  /**
   * Charge les données réelles depuis l'API Gateway et met à jour le cache local (@capacitor/preferences).
   */
  chargerDonneesReelles(): void {
    if (!this.authService.isAuthenticated() || !this.networkService.isOnline()) {
      return;
    }

    forkJoin({
      groupes: this.apiService.get<RawGroupeDto[]>('/api/v1/groupes'),
      membres: this.apiService.get<RawMembreDto[]>('/api/v1/membres'),
      reunions: this.apiService.get<RawReunionDto[]>('/api/v1/reunions'),
    })
      .pipe(
        switchMap(({ groupes, membres, reunions }) => {
          if (groupes.success && Array.isArray(groupes.data) && groupes.data.length > 0) {
            const mappedGroupes = groupes.data.map((g) => this.mapGroupe(g));
            this.groupes.set(mappedGroupes);
            const currentActiveExists = mappedGroupes.some((g) => g.id === this.activeGroupId());
            if (!currentActiveExists) {
              this.activeGroupId.set(mappedGroupes[0].id);
            }
          }

          if (membres.success && Array.isArray(membres.data) && membres.data.length > 0) {
            this.membres.set(membres.data.map((m) => this.mapMembre(m)));
          }

          if (reunions.success && Array.isArray(reunions.data) && reunions.data.length > 0) {
            const activeRaw =
              reunions.data.find((r) => r.statut === 'EN_COURS') || reunions.data[0];
            this.activeReunion.set(this.mapReunion(activeRaw));
          }

          const gid = this.activeGroupId();
          return forkJoin({
            cotisations: this.apiService.get<RawCotisationDto[]>(`/api/v1/cotisations/groupe/${gid}`),
            paiements: this.apiService.get<RawPaiementDto[]>(`/api/v1/paiements/groupe/${gid}`),
            penalites: this.apiService.get<RawPenaliteDto[]>(`/api/v1/penalites/groupe/${gid}`),
          });
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ cotisations, paiements, penalites }) => {
          if (cotisations.success && Array.isArray(cotisations.data)) {
            this.cotisations.set(cotisations.data.map((c) => this.mapCotisation(c)));
          }
          if (paiements.success && Array.isArray(paiements.data)) {
            this.paiements.set(paiements.data.map((p) => this.mapPaiement(p)));
          }
          if (penalites.success && Array.isArray(penalites.data)) {
            this.sanctions.set(penalites.data.map((s) => this.mapPenalite(s)));
          }
          this.lastSyncAt.set(new Date().toISOString());
          this.persistCachedState();
        },
      });
  }

  // Méthodes métier avec mise à jour optimiste + synchronisation Gateway / file hors-ligne persistée

  creerGroupe(input: CreerGroupeMobileInput): void {
    const isOnline = this.networkService.isOnline();
    const createurId = this.authService.currentUser()?.id || 'm-001';
    const localId = 'grp-' + Date.now();

    const nouveauGroupe: Groupe = {
      id: localId,
      nom: input.nom.trim(),
      description: input.description.trim(),
      createurMembreId: createurId,
      typeSiege: input.typeSiege,
      adresseSiegeFixe: input.typeSiege === 'FIXE' ? input.adresseSiegeFixe?.trim() : undefined,
      frequenceReunion: input.frequenceReunion,
      montantCotisationBaseXaf: input.montantCotisationBaseXaf,
      devise: 'XAF',
      nombreMembresMax: input.nombreMembresMax,
      nombreMembresActuels: 1,
      statut: 'EN_CREATION',
      premierBureauElu: false,
      soldeCaisseCashXaf: 0,
      soldeCaisseMobileMoneyXaf: 0,
      dateCreation: new Date().toISOString(),
    };

    this.groupes.update((list) => [nouveauGroupe, ...list]);
    this.activeGroupId.set(localId);
    this.persistCachedState();

    const payload: Record<string, unknown> = {
      nom: nouveauGroupe.nom,
      description: nouveauGroupe.description,
      createurMembreId: createurId,
      typeSiege: input.typeSiege,
      adresseSiege: input.typeSiege === 'FIXE' ? input.adresseSiegeFixe?.trim() || null : null,
      montantCotisationPrincipale: input.montantCotisationBaseXaf,
      frequenceReunion: input.frequenceReunion,
      nombreMembresMax: input.nombreMembresMax,
      reglementInterieur: input.reglementInterieur ?? null,
    };

    if (!isOnline) {
      this.enqueueOfflineAction('CREATE_GROUP', payload);
      return;
    }

    this.apiService
      .post<RawGroupeDto>('/api/v1/groupes', payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          if (res.success && res.data?.id) {
            const mapped = this.mapGroupe(res.data);
            this.groupes.update((list) => list.map((g) => (g.id === localId ? mapped : g)));
            if (this.activeGroupId() === localId) {
              this.activeGroupId.set(mapped.id);
            }
            this.persistCachedState();
          } else if (!res.success) {
            this.enqueueOfflineAction('CREATE_GROUP', payload);
          }
        },
        error: () => {
          this.enqueueOfflineAction('CREATE_GROUP', payload);
        },
      });
  }

  marquerPresence(membreId: string, statut: StatutPresence): void {
    const isOnline = this.networkService.isOnline();
    const reunionId = this.activeReunion().id;
    const membre = this.membres().find((m) => m.id === membreId);
    const membreNom = membre ? `${membre.prenom} ${membre.nom}` : 'Membre';
    const heureIso = new Date().toISOString();
    const heureAffichage = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    this.presences.update((list) => {
      const idx = list.findIndex((p) => p.membreId === membreId);
      if (idx >= 0) {
        const updated = [...list];
        updated[idx] = { ...updated[idx], statut, heureArrivee: heureAffichage };
        return updated;
      }
      return [
        ...list,
        {
          id: 'p-' + Date.now(),
          reunionId,
          membreId,
          membreNom,
          statut,
          heureArrivee: heureAffichage,
        },
      ];
    });
    this.persistCachedState();

    const body = {
      membreId,
      statut,
      heureArrivee: heureIso,
    };

    const queuePayload: Record<string, unknown> = {
      reunionId,
      ...body,
    };

    if (!isOnline) {
      this.enqueueOfflineAction('PRESENCE', queuePayload);
      return;
    }

    this.apiService
      .post<unknown>(`/api/v1/reunions/${reunionId}/presences`, body)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          if (!res.success) {
            this.enqueueOfflineAction('PRESENCE', queuePayload);
          }
        },
        error: () => {
          this.enqueueOfflineAction('PRESENCE', queuePayload);
        },
      });
  }

  infligerSanction(
    sanction: Omit<Penalite, 'id' | 'dateApplication' | 'statut' | 'membreNom' | 'appliqueParNom'>
  ): void {
    const isOnline = this.networkService.isOnline();
    const membre = this.membres().find((m) => m.id === sanction.membreId);
    const reunionId = sanction.reunionId ?? this.activeReunion().id;

    const nouvelle: Penalite = {
      ...sanction,
      id: 'sanc-' + Date.now(),
      reunionId,
      membreNom: membre ? `${membre.prenom} ${membre.nom}` : 'Membre',
      dateApplication: new Date().toISOString(),
      statut: 'EN_ATTENTE',
      appliqueParNom: 'Bureau exécutif',
    };
    this.sanctions.update((s) => [nouvelle, ...s]);
    this.persistCachedState();

    const payload: Record<string, unknown> = {
      membreId: sanction.membreId,
      groupeId: sanction.groupeId,
      sessionId: sanction.sessionId,
      reunionId,
      typeInfraction: sanction.typeInfraction,
      montant: sanction.montantXaf,
      motif: sanction.motif,
    };

    if (!isOnline) {
      this.enqueueOfflineAction('SANCTION', payload);
      return;
    }

    this.apiService
      .post<RawPenaliteDto>('/api/v1/penalites', payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          if (!res.success) {
            this.enqueueOfflineAction('SANCTION', payload);
          }
        },
        error: () => {
          this.enqueueOfflineAction('SANCTION', payload);
        },
      });
  }

  enregistrerPaiement(
    paiement: Omit<Paiement, 'id' | 'datePaiement' | 'statut' | 'cleIdempotence'> & {
      numeroTelephone?: string;
    }
  ): void {
    const isOnline = this.networkService.isOnline();
    const idKey = 'idemp-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
    const { numeroTelephone, ...paiementBase } = paiement;

    const nouveau: Paiement = {
      ...paiementBase,
      id: 'pay-' + Date.now(),
      datePaiement: new Date().toISOString(),
      statut: 'VALIDE',
      cleIdempotence: idKey,
    };
    this.paiements.update((p) => [nouveau, ...p]);
    this.persistCachedState();

    const isCash = paiement.modePaiement === 'CASH';
    const actionType: OfflineActionType = isCash ? 'CASH_PAYMENT' : 'MOMO_PAYMENT';
    const endpoint = isCash ? '/api/v1/paiements/cash' : '/api/v1/paiements/mobile-money';

    const payload: Record<string, unknown> = isCash
      ? {
          cotisationId: paiement.cotisationId,
          membreId: paiement.membreId,
          groupeId: paiement.groupeId,
          montant: paiement.montantXaf,
          cleIdempotence: idKey,
          pieceJointeUrl: paiement.preuveUrl || '',
          commentaire: paiement.commentaire || '',
        }
      : {
          cotisationId: paiement.cotisationId,
          membreId: paiement.membreId,
          groupeId: paiement.groupeId,
          montant: paiement.montantXaf,
          cleIdempotence: idKey,
          numeroTelephone:
            numeroTelephone || this.authService.currentUser()?.telephone || '+237699123456',
          modePaiement: paiement.modePaiement,
        };

    if (!isOnline) {
      this.enqueueOfflineAction(actionType, payload);
      return;
    }

    this.apiService
      .post<RawPaiementDto>(endpoint, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          if (!res.success) {
            this.enqueueOfflineAction(actionType, payload);
          }
        },
        error: () => {
          this.enqueueOfflineAction(actionType, payload);
        },
      });
  }

  /**
   * Rejoue séquentiellement (concatMap) chaque action hors-ligne vers son endpoint réel sur la Gateway.
   * Retire uniquement les actions synchronisées avec succès et conserve les échecs pour reprise ultérieure.
   */
  synchroniserActionsHorsLigne(): void {
    const queue = [...this.offlineQueue()];
    if (this.isSyncing() || queue.length === 0) {
      return;
    }

    if (!this.networkService.isOnline()) {
      this.syncError.set('Réseau indisponible : synchronisation reportée.');
      return;
    }

    this.isSyncing.set(true);
    this.syncError.set(null);

    const succeededIds = new Set<string>();
    let failedCount = 0;
    let lastErrorMessage: string | null = null;

    from(queue)
      .pipe(
        concatMap((action) =>
          this.replayOfflineAction(action).pipe(
            map((res) => ({
              action,
              ok: !!res.success,
              message: res.message,
            })),
            catchError(() =>
              of({
                action,
                ok: false,
                message: 'Erreur réseau lors du rejeu hors-ligne.',
              })
            )
          )
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ action, ok, message }) => {
          if (ok) {
            succeededIds.add(action.id);
            this.offlineQueue.update((current) => current.filter((item) => item.id !== action.id));
            this.persistOfflineQueue(this.offlineQueue());
          } else {
            failedCount += 1;
            lastErrorMessage = message || 'Échec de synchronisation sur une action hors-ligne.';
            this.offlineQueue.update((current) =>
              current.map((item) =>
                item.id === action.id
                  ? { ...item, retryCount: (item.retryCount ?? 0) + 1 }
                  : item
              )
            );
            this.persistOfflineQueue(this.offlineQueue());
          }
        },
        error: () => {
          this.isSyncing.set(false);
          this.syncError.set('Erreur inattendue lors de la synchronisation hors-ligne.');
        },
        complete: () => {
          if (succeededIds.size > 0) {
            this.offlineQueue.update((current) =>
              current.filter((item) => !succeededIds.has(item.id))
            );
            this.persistOfflineQueue(this.offlineQueue());
          }

          this.isSyncing.set(false);
          if (failedCount > 0) {
            this.syncError.set(
              lastErrorMessage || `${failedCount} action(s) en attente de nouvelle tentative.`
            );
          } else {
            this.syncError.set(null);
            this.lastSyncAt.set(new Date().toISOString());
          }
        },
      });
  }

  private replayOfflineAction(action: OfflineAction): Observable<ApiResponse<unknown>> {
    switch (action.type) {
      case 'PRESENCE': {
        const reunionId = String(action.payload['reunionId'] || this.activeReunion().id);
        return this.apiService.post<unknown>(`/api/v1/reunions/${reunionId}/presences`, {
          membreId: action.payload['membreId'],
          statut: action.payload['statut'],
          heureArrivee: action.payload['heureArrivee'],
        });
      }
      case 'CASH_PAYMENT':
        return this.apiService.post<unknown>('/api/v1/paiements/cash', action.payload);
      case 'MOMO_PAYMENT':
        return this.apiService.post<unknown>('/api/v1/paiements/mobile-money', action.payload);
      case 'SANCTION':
        return this.apiService.post<unknown>('/api/v1/penalites', action.payload);
      case 'CREATE_GROUP':
        return this.apiService.post<unknown>('/api/v1/groupes', action.payload);
    }
  }

  private enqueueOfflineAction(type: OfflineActionType, payload: Record<string, unknown>): void {
    const action: OfflineAction = {
      id: 'offline-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7),
      type,
      payload,
      timestamp: new Date().toISOString(),
      retryCount: 0,
    };
    this.offlineQueue.update((q) => {
      const nextQueue = [...q, action];
      this.persistOfflineQueue(nextQueue);
      return nextQueue;
    });
  }

  private persistOfflineQueue(queue: OfflineAction[]): void {
    from(Preferences.set({ key: OFFLINE_QUEUE_KEY, value: JSON.stringify(queue) }))
      .pipe(
        catchError(() => of(undefined)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }

  private persistCachedState(): void {
    const snapshot: CachedTontineState = {
      groupes: this.groupes(),
      membres: this.membres(),
      cotisations: this.cotisations(),
      paiements: this.paiements(),
      sanctions: this.sanctions(),
      presences: this.presences(),
      cachedAt: new Date().toISOString(),
    };

    from(Preferences.set({ key: CACHED_STATE_KEY, value: JSON.stringify(snapshot) }))
      .pipe(
        catchError(() => of(undefined)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }

  private mapGroupe(g: RawGroupeDto): Groupe {
    return {
      id: g.id,
      nom: g.nom,
      description: g.description || '',
      createurMembreId: g.createurMembreId || 'm-001',
      typeSiege: g.typeSiege || 'FIXE',
      adresseSiegeFixe: g.adresseSiegeFixe ?? g.adresseSiege,
      frequenceReunion: g.frequenceReunion || 'MENSUELLE',
      montantCotisationBaseXaf: g.montantCotisationBaseXaf ?? g.montantCotisationPrincipale ?? 50000,
      devise: 'XAF',
      nombreMembresMax: g.nombreMembresMax ?? 15,
      nombreMembresActuels: g.nombreMembresActuels ?? 1,
      statut: g.statut || 'ACTIF',
      premierBureauElu: g.premierBureauElu ?? false,
      soldeCaisseCashXaf: g.soldeCaisseCashXaf ?? 0,
      soldeCaisseMobileMoneyXaf: g.soldeCaisseMobileMoneyXaf ?? 0,
      dateCreation: g.dateCreation || g.createdAt || new Date().toISOString(),
    };
  }

  private mapMembre(m: RawMembreDto): Membre {
    const nowIso = new Date().toISOString();
    return {
      id: m.id,
      authUtilisateurId: m.authUtilisateurId ?? m.id,
      nom: m.nom,
      prenom: m.prenom,
      telephone: m.telephone || '',
      email: m.email || '',
      avatarUrl: m.avatarUrl ?? m.photoUrl,
      photoUrl: m.photoUrl ?? m.avatarUrl,
      ville: m.ville || 'Douala',
      pays: m.pays || 'Cameroun',
      statut: m.statut || 'ACTIF',
      dateInscription: (m.dateInscription || m.createdAt || nowIso).substring(0, 10),
      createdAt: m.createdAt || nowIso,
      estVerifieKyc: m.estVerifieKyc ?? false,
    };
  }

  private mapReunion(r: RawReunionDto): Reunion {
    return {
      id: r.id,
      groupeId: r.groupeId,
      sessionId: r.sessionId || r.sessionTontineId || this.activeSession().id,
      numeroOrdre: 1,
      titre: r.titre,
      dateReunion: (r.dateReunion || new Date().toISOString()).substring(0, 10),
      heureDebut: r.heureDebut || '14:00',
      typeSiege: r.typeSiege || 'FIXE',
      hoteMembreId: r.hoteMembreId ?? r.hoteId,
      lieuAdresse: r.lieuAdresse || r.lieuReunion || 'Siège du groupe',
      ordreDuJour: r.ordreDuJour || (r.ordreJour ? r.ordreJour.split('\n') : []),
      presences: this.presences(),
      totalCollecteSeanceXaf: 0,
      totalAmendesSeanceXaf: 0,
      statut: r.statut || 'EN_COURS',
    };
  }

  private mapCotisation(c: RawCotisationDto): Cotisation {
    const membre = this.membres().find((m) => m.id === c.membreId);
    return {
      id: c.id,
      groupeId: c.groupeId,
      sessionId: c.sessionId || this.activeSession().id,
      reunionId: c.reunionId,
      membreId: c.membreId,
      membreNom: c.membreNom || (membre ? `${membre.prenom} ${membre.nom}` : 'Membre'),
      typeCotisation: c.typeCotisation || 'ROTATIVE_POT',
      montantAttenduXaf: c.montantAttenduXaf ?? c.montantAttendu ?? 0,
      montantPayeXaf: c.montantPayeXaf ?? c.montantPaye ?? 0,
      dateEcheance: (c.dateEcheance || new Date().toISOString()).substring(0, 10),
      statut: c.statut || 'EN_ATTENTE',
    };
  }

  private mapPaiement(p: RawPaiementDto): Paiement {
    const membre = this.membres().find((m) => m.id === p.membreId);
    return {
      id: p.id,
      cotisationId: p.cotisationId,
      groupeId: p.groupeId,
      sessionId: p.sessionId || this.activeSession().id,
      membreId: p.membreId,
      membreNom: p.membreNom || (membre ? `${membre.prenom} ${membre.nom}` : 'Membre'),
      montantXaf: p.montantXaf ?? p.montant ?? 0,
      modePaiement: p.modePaiement || 'CASH',
      typeCotisation: p.typeCotisation || 'ROTATIVE_POT',
      statut: p.statut || 'VALIDE',
      cleIdempotence: p.cleIdempotence || 'idemp-' + p.id,
      referenceTransaction: p.referenceTransaction,
      preuveUrl: p.preuveUrl ?? p.pieceJointeUrl,
      commentaire: p.commentaire,
      valideParTresorierId: p.valideParTresorierId,
      datePaiement: p.datePaiement || p.createdAt || new Date().toISOString(),
    };
  }

  private mapPenalite(s: RawPenaliteDto): Penalite {
    const membre = this.membres().find((m) => m.id === s.membreId);
    return {
      id: s.id,
      groupeId: s.groupeId,
      sessionId: s.sessionId || this.activeSession().id,
      reunionId: s.reunionId,
      membreId: s.membreId,
      membreNom: s.membreNom || (membre ? `${membre.prenom} ${membre.nom}` : 'Membre'),
      typeInfraction: s.typeInfraction || 'RETARD_REUNION',
      motif: s.motif || '',
      montantXaf: s.montantXaf ?? s.montant ?? 0,
      statut: s.statut || 'EN_ATTENTE',
      dateApplication: s.dateApplication || s.createdAt || new Date().toISOString(),
      dateReglement: s.dateReglement,
      appliqueParNom: s.appliqueParNom || 'Bureau exécutif',
    };
  }
}
