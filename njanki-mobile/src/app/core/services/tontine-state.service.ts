import { Injectable, signal, computed, inject } from '@angular/core';
import { GroupeTontine, SessionTontine, MandatBureau } from '../../shared/models/groupe.model';
import { Membre, RoleMembre } from '../../shared/models/membre.model';
import { Cotisation, TypeCotisation, TourPot } from '../../shared/models/cotisation.model';
import { Paiement, ModePaiement } from '../../shared/models/paiement.model';
import { Reunion, PresenceSeance, StatutPresence } from '../../shared/models/reunion.model';
import { Sanction, BaremePenalite } from '../../shared/models/penalite.model';
import { NetworkService } from './network.service';

export interface OfflineAction {
  id: string;
  type: 'PRESENCE' | 'CASH_PAYMENT' | 'SANCTION';
  payload: Record<string, unknown>;
  timestamp: string;
}

@Injectable({
  providedIn: 'root',
})
export class MobileTontineStateService {
  private readonly networkService = inject(NetworkService);

  // File d'attente d'actions hors-ligne (mode dégradé 3G)
  readonly offlineQueue = signal<OfflineAction[]>([]);

  // Groupes
  readonly groupes = signal<GroupeTontine[]>([
    {
      id: 'grp-001',
      nom: 'Tontine des Élites de Bafoussam - Yaoundé',
      description: 'Solidarité, épargne rotative et entraide mutuelle.',
      typeSiege: 'FIXE',
      adresseSiegeFixe: 'Maison du Peuple, Quartier Bastos, Yaoundé',
      devise: 'XAF',
      statut: 'ACTIF',
      dateCreation: '2024-01-15T10:00:00Z',
      montantCotisationBaseXaf: 50000,
      frequence: 'MENSUEL',
      jourSeance: '1er Samedi du mois',
      reglementInterieurUrl: '/docs/reglement-bafoussam.pdf',
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
    dateDebut: '2025-01-01',
    dateFinPrevue: '2027-01-01',
    presidentId: 'm-002',
    tresorierId: 'm-003',
    secretaireId: 'm-004',
    commissaireAuxComptesId: 'm-005',
    statut: 'EN_COURS',
  });

  readonly activeSession = signal<SessionTontine>({
    id: 'ses-2026',
    groupeId: 'grp-001',
    anneeOuLabel: 'Cycle Annuel 2026',
    dateDebut: '2026-01-01',
    dateFin: '2026-12-31',
    montantPotMensuelXaf: 600000,
    statut: 'OUVERTE',
    nombreToursPrevu: 12,
    tourActuel: 3,
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
      createdAt: '2024-01-15T10:00:00Z',
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
      createdAt: '2024-01-15T10:00:00Z',
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
      createdAt: '2024-01-15T10:00:00Z',
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
      createdAt: '2024-01-15T10:00:00Z',
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
      createdAt: '2024-01-15T10:00:00Z',
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

  // Réunion active (Séance live)
  readonly activeReunion = signal<Reunion>({
    id: 'reu-live-03',
    groupeId: 'grp-001',
    sessionId: 'ses-2026',
    dateHeureDebut: '2026-03-07T14:00:00Z',
    lieu: 'Maison du Peuple, Yaoundé',
    statut: 'EN_COURS',
    presidentSeanceId: 'm-002',
    secretaireSeanceId: 'm-004',
    hoteId: 'm-003',
    ordreDuJour: [
      '1. Accueil et installation des membres',
      '2. Appel nominatif et vérification du quorum',
      '3. Collecte des cotisations (Pot rotatif + Secours + Réserve)',
      '4. Attribution et décaissement du Pot du mois à Mme Clarisse Nguemo',
      '5. Traitement des cas sociaux et sanctions de séance',
      '6. Divers et clôture',
    ],
    beneficiairePotDuJourId: 'm-003',
  });

  // Présences
  readonly presences = signal<PresenceSeance[]>([
    { id: 'p-1', reunionId: 'reu-live-03', membreId: 'm-001', membreNom: 'Jean-Paul FOKOU', statut: 'PRESENT', heureArrivee: '13:50' },
    { id: 'p-2', reunionId: 'reu-live-03', membreId: 'm-002', membreNom: 'Maurice KAMDEM', statut: 'PRESENT', heureArrivee: '13:45' },
    { id: 'p-3', reunionId: 'reu-live-03', membreId: 'm-003', membreNom: 'Clarisse NGUEMO', statut: 'PRESENT', heureArrivee: '13:55' },
    { id: 'p-4', reunionId: 'reu-live-03', membreId: 'm-004', membreNom: 'Alain TCHOUA', statut: 'RETARD', heureArrivee: '14:25' },
    { id: 'p-5', reunionId: 'reu-live-03', membreId: 'm-005', membreNom: 'Béatrice KENMOE', statut: 'EXCUSE', motifAbsence: 'Mission professionnelle à Douala' },
  ]);

  // Sanctions & Barème
  readonly bareme = signal<BaremePenalite[]>([
    { typeInfraction: 'RETARD_REUNION', intitule: 'Retard à la réunion (> 15 min)', montantParDefautXaf: 1000, actif: true },
    { typeInfraction: 'ABSENCE_NON_JUSTIFIEE', intitule: 'Absence non justifiée', montantParDefautXaf: 5000, actif: true },
    { typeInfraction: 'BAVARDAGE_SONNERIE', intitule: 'Sonnerie téléphone / Trouble de séance', montantParDefautXaf: 500, actif: true },
    { typeInfraction: 'RETARD_PAIEMENT', intitule: 'Retard versement cotisation', montantParDefautXaf: 2000, actif: true },
  ]);

  readonly sanctions = signal<Sanction[]>([
    {
      id: 'sanc-001',
      groupeId: 'grp-001',
      reunionId: 'reu-live-03',
      membreId: 'm-004',
      typeInfraction: 'RETARD_REUNION',
      montantXaf: 1000,
      motif: 'Arrivée à 14h25 au lieu de 14h00',
      statut: 'PAYE',
      dateSanction: '2026-03-07T14:30:00Z',
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
      .filter((p) => (p.modePaiement === 'MTN_MOMO' || p.modePaiement === 'ORANGE_MONEY') && p.statut === 'VALIDE')
      .reduce((acc, curr) => acc + curr.montantXaf, 0);
  });

  readonly totalTresorerieGlobale = computed(() => {
    return this.totalCaissePhysiqueCash() + this.totalMobileMoney();
  });

  // Méthodes métier
  marquerPresence(membreId: string, statut: StatutPresence): void {
    const isOnline = this.networkService.isOnline();
    this.presences.update((list) => {
      const idx = list.findIndex((p) => p.membreId === membreId);
      if (idx >= 0) {
        const updated = [...list];
        updated[idx] = { ...updated[idx], statut, heureArrivee: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) };
        return updated;
      }
      return [
        ...list,
        {
          id: 'p-' + Date.now(),
          reunionId: this.activeReunion().id,
          membreId,
          statut,
          heureArrivee: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ];
    });

    if (!isOnline) {
      this.enqueueOfflineAction('PRESENCE', { membreId, statut });
    }
  }

  infligerSanction(sanction: Omit<Sanction, 'id' | 'dateSanction' | 'statut'>): void {
    const isOnline = this.networkService.isOnline();
    const nouvelle: Sanction = {
      ...sanction,
      id: 'sanc-' + Date.now(),
      dateSanction: new Date().toISOString(),
      statut: 'NON_PAYE',
    };
    this.sanctions.update((s) => [nouvelle, ...s]);

    if (!isOnline) {
      this.enqueueOfflineAction('SANCTION', { ...nouvelle });
    }
  }

  enregistrerPaiement(paiement: Omit<Paiement, 'id' | 'datePaiement' | 'statut' | 'cleIdempotence'>): void {
    const isOnline = this.networkService.isOnline();
    const idKey = 'idemp-' + Date.now() + '-' + Math.random().toString(36).substring(7);
    const nouveau: Paiement = {
      ...paiement,
      id: 'pay-' + Date.now(),
      datePaiement: new Date().toISOString(),
      statut: 'VALIDE',
      cleIdempotence: idKey,
    };
    this.paiements.update((p) => [nouveau, ...p]);

    if (!isOnline) {
      this.enqueueOfflineAction('CASH_PAYMENT', { ...nouveau });
    }
  }

  private enqueueOfflineAction(type: OfflineAction['type'], payload: Record<string, unknown>): void {
    const action: OfflineAction = {
      id: 'offline-' + Date.now(),
      type,
      payload,
      timestamp: new Date().toISOString(),
    };
    this.offlineQueue.update((q) => [...q, action]);
  }

  synchroniserActionsHorsLigne(): void {
    this.offlineQueue.set([]);
  }
}
