import { Injectable, signal, computed, inject } from '@angular/core';
import { AuthService } from './auth.service';
import { Groupe, MandatBureau, SessionTontine, TypeSiege, FrequenceReunion } from '@shared/models/groupe.model';
import { Membre, RoleMembre } from '@shared/models/membre.model';
import { Cotisation, TourPot, TypeCotisation } from '@shared/models/cotisation.model';
import { Paiement, EnregistrerPaiementRequest } from '@shared/models/paiement.model';
import { Reunion, StatutPresence } from '@shared/models/reunion.model';
import { Penalite, TarificationPenalite, TypeInfraction } from '@shared/models/penalite.model';

@Injectable({
  providedIn: 'root'
})
export class TontineStateService {
  private readonly authService = inject(AuthService);

  // Jeu de données initial réaliste (Cameroun Njangi)
  private readonly initialMembres: Membre[] = [
    {
      id: 'usr-1',
      nom: 'Mbarga',
      prenom: 'Jean-Paul',
      telephone: '+237699123456',
      email: 'jp.mbarga@njangi.cm',
      avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?fit=crop&w=256&h=256&q=80',
      ville: 'Douala',
      pays: 'Cameroun',
      dateInscription: '2025-01-15',
      estVerifieKyc: true,
    },
    {
      id: 'usr-2',
      nom: 'Ngo Bikoï',
      prenom: 'Chantal',
      telephone: '+237677889900',
      email: 'chantal.ngo@njangi.cm',
      avatarUrl: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?fit=crop&w=256&h=256&q=80',
      ville: 'Douala',
      pays: 'Cameroun',
      dateInscription: '2025-01-20',
      estVerifieKyc: true,
    },
    {
      id: 'usr-3',
      nom: 'Kamga',
      prenom: 'Pierre',
      telephone: '+237655443322',
      email: 'pierre.kamga@njangi.cm',
      avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?fit=crop&w=256&h=256&q=80',
      ville: 'Douala',
      pays: 'Cameroun',
      dateInscription: '2025-01-22',
      estVerifieKyc: true,
    },
    {
      id: 'usr-4',
      nom: 'Eto\'o Fils',
      prenom: 'Samuel',
      telephone: '+237699001122',
      email: 'samuel.etoo@njangi.cm',
      avatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?fit=crop&w=256&h=256&q=80',
      ville: 'Yaoundé',
      pays: 'Cameroun',
      dateInscription: '2025-01-10',
      estVerifieKyc: true,
    },
    {
      id: 'usr-5',
      nom: 'Fokou',
      prenom: 'Rodrigue',
      telephone: '+237670112233',
      email: 'rodrigue.fokou@njangi.cm',
      avatarUrl: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?fit=crop&w=256&h=256&q=80',
      ville: 'Douala',
      pays: 'Cameroun',
      dateInscription: '2025-02-05',
      estVerifieKyc: true,
    },
    {
      id: 'usr-6',
      nom: 'Fotso',
      prenom: 'Christelle',
      telephone: '+237691223344',
      email: 'christelle.fotso@njangi.cm',
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?fit=crop&w=256&h=256&q=80',
      ville: 'Bafoussam',
      pays: 'Cameroun',
      dateInscription: '2025-02-10',
      estVerifieKyc: true,
    }
  ];

  private readonly initialMandat1: MandatBureau = {
    id: 'man-1',
    groupeId: 'grp-1',
    presidentMembreId: 'usr-1',
    presidentNom: 'Jean-Paul Mbarga',
    tresorierMembreId: 'usr-2',
    tresorierNom: 'Chantal Ngo Bikoï',
    secretaireMembreId: 'usr-3',
    secretaireNom: 'Pierre Kamga',
    dateDebut: '2025-02-01',
    dateFinPrevue: '2027-01-31',
    estActif: true,
  };

  private readonly initialSession1: SessionTontine = {
    id: 'ses-1',
    groupeId: 'grp-1',
    numeroSession: 2,
    libelle: 'Session Annuelle 2026-2027',
    dateDebut: '2026-02-01',
    dateFinPrevue: '2027-01-31',
    montantPotParTourXaf: 100000,
    montantSecoursXaf: 5000,
    montantReserveXaf: 2000,
    estActive: true,
    totalCollecteXaf: 1284000,
    totalDistribueXaf: 600000,
  };

  private readonly initialGroupes: Groupe[] = [
    {
      id: 'grp-1',
      nom: 'Solidarité Douala Akwa',
      description: 'Tontine d\'entraide et épargne rotative pour entrepreneurs et cadres du Littoral',
      createurMembreId: 'usr-4', // Samuel Eto'o est le créateur
      typeSiege: 'ROTATIF', // Siège rotatif chez les membres
      ordreRotationSiege: ['usr-1', 'usr-2', 'usr-3', 'usr-5'],
      frequenceReunion: 'MENSUELLE',
      devise: 'XAF',
      nombreMembresMax: 15,
      nombreMembresActuels: 6,
      statut: 'ACTIF',
      premierBureauElu: true, // Le créateur a été rétrogradé, le bureau est élu !
      mandatActif: this.initialMandat1,
      sessionActive: this.initialSession1,
      soldeCaisseCashXaf: 450000,
      soldeCaisseMobileMoneyXaf: 834000,
      dateCreation: '2025-01-15',
    },
    {
      id: 'grp-2',
      nom: 'Njangi Diaspora Yaoundé',
      description: 'Réseau d\'investissement et caisse d\'entraide communautaire',
      createurMembreId: 'usr-1',
      typeSiege: 'FIXE',
      adresseSiegeFixe: 'Maison de la Culture Bamiléké, Bastos, Yaoundé',
      frequenceReunion: 'BIMENSUELLE',
      devise: 'XAF',
      nombreMembresMax: 20,
      nombreMembresActuels: 12,
      statut: 'ACTIF',
      premierBureauElu: true,
      soldeCaisseCashXaf: 320000,
      soldeCaisseMobileMoneyXaf: 1250000,
      dateCreation: '2025-03-01',
    },
    {
      id: 'grp-3',
      nom: 'Tontine Jeunes Entrepreneurs',
      description: 'Groupe en cours d\'initialisation, en attente de l\'élection du premier bureau',
      createurMembreId: 'usr-1', // Jean-Paul Mbarga est ici le Créateur avec pouvoirs techniques
      typeSiege: 'FIXE',
      adresseSiegeFixe: 'Immeuble Kouam, Rue Castelnau, Akwa',
      frequenceReunion: 'MENSUELLE',
      devise: 'XAF',
      nombreMembresMax: 10,
      nombreMembresActuels: 3,
      statut: 'EN_CREATION',
      premierBureauElu: false, // Pas encore élu : le créateur a les droits initiaux
      soldeCaisseCashXaf: 0,
      soldeCaisseMobileMoneyXaf: 0,
      dateCreation: '2026-01-05',
    }
  ];

  // Cotisations initiales (multi-caisses simultanées)
  private readonly initialCotisations: Cotisation[] = [
    {
      id: 'cot-1',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-1',
      membreNom: 'Jean-Paul Mbarga',
      typeCotisation: 'ROTATIVE_POT',
      montantAttenduXaf: 100000,
      montantPayeXaf: 100000,
      dateEcheance: '2026-09-15',
      datePaiement: '2026-09-10',
      statut: 'PAYE',
    },
    {
      id: 'cot-2',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-1',
      membreNom: 'Jean-Paul Mbarga',
      typeCotisation: 'SECOURS_DECES',
      montantAttenduXaf: 5000,
      montantPayeXaf: 5000,
      dateEcheance: '2026-09-15',
      datePaiement: '2026-09-10',
      statut: 'PAYE',
    },
    {
      id: 'cot-3',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-2',
      membreNom: 'Chantal Ngo Bikoï',
      typeCotisation: 'ROTATIVE_POT',
      montantAttenduXaf: 100000,
      montantPayeXaf: 100000,
      dateEcheance: '2026-09-15',
      datePaiement: '2026-09-11',
      statut: 'PAYE',
    },
    {
      id: 'cot-4',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-3',
      membreNom: 'Pierre Kamga',
      typeCotisation: 'ROTATIVE_POT',
      montantAttenduXaf: 100000,
      montantPayeXaf: 0,
      dateEcheance: '2026-09-15',
      statut: 'EN_ATTENTE',
    },
    {
      id: 'cot-5',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-5',
      membreNom: 'Rodrigue Fokou',
      typeCotisation: 'ROTATIVE_POT',
      montantAttenduXaf: 100000,
      montantPayeXaf: 0,
      dateEcheance: '2026-09-10',
      statut: 'EN_RETARD',
      penaliteAppliqueeXaf: 2000,
    },
    {
      id: 'cot-6',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-6',
      membreNom: 'Christelle Fotso',
      typeCotisation: 'ROTATIVE_POT',
      montantAttenduXaf: 100000,
      montantPayeXaf: 0,
      dateEcheance: '2026-09-05',
      statut: 'PENALISE',
      penaliteAppliqueeXaf: 5000,
    }
  ];

  // Ordre de passage de la cagnotte (le Pot)
  private readonly initialToursPot: TourPot[] = [
    {
      id: 'pot-1',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      numeroTour: 1,
      beneficiaireMembreId: 'usr-2',
      beneficiaireNom: 'Chantal Ngo Bikoï',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 600000,
      dateAttribution: '2026-08-15',
      dateVersementEffective: '2026-08-16',
      statut: 'VERSE',
      modeVersement: 'MTN Mobile Money',
      observations: 'Versement complet validé en séance sans retenue.',
    },
    {
      id: 'pot-2',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      numeroTour: 2,
      beneficiaireMembreId: 'usr-1',
      beneficiaireNom: 'Jean-Paul Mbarga',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 500000,
      dateAttribution: '2026-09-15',
      statut: 'PRET_POUR_VERSEMENT',
      observations: 'Tour actuel ! En attente de finalisation des paiements de 2 membres.',
    },
    {
      id: 'pot-3',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      numeroTour: 3,
      beneficiaireMembreId: 'usr-3',
      beneficiaireNom: 'Pierre Kamga',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 0,
      dateAttribution: '2026-10-15',
      statut: 'A_VENIR',
    },
    {
      id: 'pot-4',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      numeroTour: 4,
      beneficiaireMembreId: 'usr-5',
      beneficiaireNom: 'Rodrigue Fokou',
      montantTheoriqueXaf: 600000,
      montantReelVerseXaf: 0,
      dateAttribution: '2026-11-15',
      statut: 'A_VENIR',
    }
  ];

  // Réunions
  private readonly initialReunions: Reunion[] = [
    {
      id: 'reu-1',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      numeroOrdre: 8,
      titre: 'Séance Ordinaire de Septembre 2026',
      dateReunion: '2026-09-15',
      heureDebut: '17:30',
      heureFin: '20:00',
      typeSiege: 'ROTATIF',
      hoteMembreId: 'usr-2',
      hoteNom: 'Mme Chantal Ngo Bikoï',
      lieuAdresse: 'Résidence Ngo, Rue des Palmiers, Bonapriso, Douala',
      ordreDuJour: [
        '1. Ouverture de séance & prière',
        '2. Appel & vérification du quorum',
        '3. Lecture & approbation du PV précédent',
        '4. Collecte des cotisations (Pot, Secours, Réserve)',
        '5. Décaissement du pot à M. Jean-Paul Mbarga',
        '6. Examen des demandes de prêt de secours',
        '7. Divers & fixation de la prochaine réunion'
      ],
      presences: [
        { membreId: 'usr-1', membreNom: 'Jean-Paul Mbarga', statut: 'PRESENT', heureArrivee: '17:25' },
        { membreId: 'usr-2', membreNom: 'Chantal Ngo Bikoï', statut: 'PRESENT', heureArrivee: '17:15' },
        { membreId: 'usr-3', membreNom: 'Pierre Kamga', statut: 'RETARD', heureArrivee: '18:10', sanctionAppliquee: true },
        { membreId: 'usr-4', membreNom: 'Samuel Eto\'o Fils', statut: 'ABSENT_JUSTIFIE', motifAbsence: 'Voyage officiel CAF' },
        { membreId: 'usr-5', membreNom: 'Rodrigue Fokou', statut: 'PRESENT', heureArrivee: '17:30' },
        { membreId: 'usr-6', membreNom: 'Christelle Fotso', statut: 'ABSENT_NON_JUSTIFIE', sanctionAppliquee: true }
      ],
      totalCollecteSeanceXaf: 214000,
      totalAmendesSeanceXaf: 6000,
      compteRenduRedige: 'Séance ouverte à 17h35 par le Président Mbarga. Quorum atteint avec 4 membres présents physiquement. Mme Ngo a reçu ses félicitations pour l\'accueil au siège rotatif. Les cotisations ont été enregistrées avec rigueur par la Trésorière.',
      statut: 'EN_COURS',
    },
    {
      id: 'reu-2',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      numeroOrdre: 7,
      titre: 'Séance Ordinaire d\'Août 2026',
      dateReunion: '2026-08-15',
      heureDebut: '17:00',
      heureFin: '19:45',
      typeSiege: 'ROTATIF',
      hoteMembreId: 'usr-1',
      hoteNom: 'M. Jean-Paul Mbarga',
      lieuAdresse: 'Domicile Mbarga, Bali, Douala',
      ordreDuJour: [
        '1. Ouverture de séance',
        '2. Présences',
        '3. Décaissement du pot à Mme Ngo Bikoï (600 000 XAF)',
        '4. Clôture'
      ],
      presences: [
        { membreId: 'usr-1', membreNom: 'Jean-Paul Mbarga', statut: 'PRESENT' },
        { membreId: 'usr-2', membreNom: 'Chantal Ngo Bikoï', statut: 'PRESENT' },
        { membreId: 'usr-3', membreNom: 'Pierre Kamga', statut: 'PRESENT' },
        { membreId: 'usr-5', membreNom: 'Rodrigue Fokou', statut: 'PRESENT' },
      ],
      totalCollecteSeanceXaf: 614000,
      totalAmendesSeanceXaf: 1000,
      compteRenduRedige: 'La séance s\'est tenue dans une ambiance fraternelle. Versement intégral du pot de 600 000 XAF à Mme Ngo Bikoï par Mobile Money, avec quittance signée par la Trésorière.',
      statut: 'CLOTUREE',
    }
  ];

  // Paiements enregistrés (avec preuves reçus signés)
  private readonly initialPaiements: Paiement[] = [
    {
      id: 'pay-1',
      cotisationId: 'cot-1',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      membreId: 'usr-1',
      membreNom: 'Jean-Paul Mbarga',
      montantXaf: 100000,
      modePaiement: 'CASH',
      typeCotisation: 'ROTATIVE_POT',
      statut: 'VALIDE',
      cleIdempotence: 'idemp-pay-001',
      preuveUrl: 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?fit=crop&w=600&h=400&q=80',
      commentaire: 'Remise en espèces à la Trésorière Ngo Bikoï',
      valideParTresorierId: 'usr-2',
      datePaiement: '2026-09-10 14:20',
      dateValidation: '2026-09-10 14:25',
    },
    {
      id: 'pay-2',
      cotisationId: 'cot-2',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      membreId: 'usr-1',
      membreNom: 'Jean-Paul Mbarga',
      montantXaf: 5000,
      modePaiement: 'MTN_MOMO',
      typeCotisation: 'SECOURS_DECES',
      statut: 'VALIDE',
      cleIdempotence: 'idemp-pay-002',
      referenceTransaction: 'MTN-CI-20260910-88902',
      datePaiement: '2026-09-10 14:30',
      dateValidation: '2026-09-10 14:31',
    },
    {
      id: 'pay-3',
      cotisationId: 'cot-3',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      membreId: 'usr-2',
      membreNom: 'Chantal Ngo Bikoï',
      montantXaf: 100000,
      modePaiement: 'ORANGE_MONEY',
      typeCotisation: 'ROTATIVE_POT',
      statut: 'VALIDE',
      cleIdempotence: 'idemp-pay-003',
      referenceTransaction: 'OM-CM-99482103',
      datePaiement: '2026-09-11 09:15',
      dateValidation: '2026-09-11 09:16',
    }
  ];

  // Pénalités
  private readonly initialPenalites: Penalite[] = [
    {
      id: 'pen-1',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-3',
      membreNom: 'Pierre Kamga',
      typeInfraction: 'RETARD_REUNION',
      motif: 'Arrivée à 18h10 après début officiel à 17h30',
      montantXaf: 1000,
      statut: 'PAYEE',
      dateApplication: '2026-09-15 18:12',
      dateReglement: '2026-09-15 18:15',
      appliqueParNom: 'Jean-Paul Mbarga (Président)',
    },
    {
      id: 'pen-2',
      groupeId: 'grp-1',
      sessionId: 'ses-1',
      reunionId: 'reu-1',
      membreId: 'usr-6',
      membreNom: 'Christelle Fotso',
      typeInfraction: 'ABSENCE_NON_JUSTIFIEE',
      motif: 'Absence sans justification ni excuse adressée au Secrétaire',
      montantXaf: 5000,
      statut: 'EN_ATTENTE',
      dateApplication: '2026-09-15 18:00',
      appliqueParNom: 'Jean-Paul Mbarga (Président)',
    }
  ];

  private readonly initialTarifications: TarificationPenalite[] = [
    { id: 'tar-1', groupeId: 'grp-1', typeInfraction: 'RETARD_REUNION', libelle: 'Retard à la réunion (> 15 min)', montantForfaitaireXaf: 1000, estPourcentage: false },
    { id: 'tar-2', groupeId: 'grp-1', typeInfraction: 'BAVARDAGE_SONNERIE', libelle: 'Sonnerie de téléphone ou bavardage en séance', montantForfaitaireXaf: 500, estPourcentage: false },
    { id: 'tar-3', groupeId: 'grp-1', typeInfraction: 'ABSENCE_NON_JUSTIFIEE', libelle: 'Absence sans mot d\'excuse', montantForfaitaireXaf: 5000, estPourcentage: false },
    { id: 'tar-4', groupeId: 'grp-1', typeInfraction: 'RETARD_PAIEMENT', libelle: 'Retard de cotisation après échéance', montantForfaitaireXaf: 2000, estPourcentage: false },
    { id: 'tar-5', groupeId: 'grp-1', typeInfraction: 'NON_RESPECT_REGLE', libelle: 'Non-respect du code de conduite', montantForfaitaireXaf: 2500, estPourcentage: false }
  ];

  // SIGNALS PRINCIPAUX (État réactif pur)
  readonly membres = signal<Membre[]>(this.initialMembres);
  readonly groupes = signal<Groupe[]>(this.initialGroupes);
  readonly cotisations = signal<Cotisation[]>(this.initialCotisations);
  readonly toursPot = signal<TourPot[]>(this.initialToursPot);
  readonly reunions = signal<Reunion[]>(this.initialReunions);
  readonly paiements = signal<Paiement[]>(this.initialPaiements);
  readonly penalites = signal<Penalite[]>(this.initialPenalites);
  readonly tarifications = signal<TarificationPenalite[]>(this.initialTarifications);

  // Groupe actif sélectionné
  readonly groupeActif = computed<Groupe>(() => {
    const gid = this.authService.groupeActifId();
    return this.groupes().find(g => g.id === gid) || this.groupes()[0];
  });

  // Rôle actif de l'utilisateur connecté dans le groupe actif
  readonly roleActif = computed<RoleMembre>(() => {
    const grp = this.groupeActif();
    const adhesions = this.authService.adhesions();
    const adh = adhesions.find(a => a.groupeId === grp.id);
    return adh?.role || 'MEMBRE';
  });

  // Session active du groupe courant
  readonly sessionActive = computed<SessionTontine | undefined>(() => {
    return this.groupeActif().sessionActive;
  });

  // Mandat actif du groupe courant
  readonly mandatActif = computed<MandatBureau | undefined>(() => {
    return this.groupeActif().mandatActif;
  });

  // Statistiques calculées dynamiquement pour le groupe actif
  readonly statsGroupeActif = computed(() => {
    const grp = this.groupeActif();
    const cots = this.cotisations().filter(c => c.groupeId === grp.id);
    const pens = this.penalites().filter(p => p.groupeId === grp.id);
    
    const totalCollecte = cots.reduce((acc, c) => acc + c.montantPayeXaf, 0);
    const totalAttendu = cots.reduce((acc, c) => acc + c.montantAttenduXaf, 0);
    const tauxCollecte = totalAttendu > 0 ? Math.round((totalCollecte / totalAttendu) * 100) : 0;
    
    const totalPenalitesPayees = pens.filter(p => p.statut === 'PAYEE').reduce((acc, p) => acc + p.montantXaf, 0);
    const totalPenalitesImpayees = pens.filter(p => p.statut === 'EN_ATTENTE').reduce((acc, p) => acc + p.montantXaf, 0);
    
    const membresEnRetard = cots.filter(c => c.statut === 'EN_RETARD' || c.statut === 'PENALISE');

    return {
      totalCollecte,
      totalAttendu,
      tauxCollecte,
      totalPenalitesPayees,
      totalPenalitesImpayees,
      soldeCash: grp.soldeCaisseCashXaf,
      soldeMobileMoney: grp.soldeCaisseMobileMoneyXaf,
      soldeTotal: grp.soldeCaisseCashXaf + grp.soldeCaisseMobileMoneyXaf,
      membresEnRetardCount: membresEnRetard.length,
      membresEnRetard,
    };
  });

  // Cotisations de l'utilisateur connecté dans le groupe actif
  readonly mesCotisations = computed<Cotisation[]>(() => {
    const uid = this.authService.utilisateur().id;
    const gid = this.groupeActif().id;
    return this.cotisations().filter(c => c.membreId === uid && c.groupeId === gid);
  });

  // Prochaine réunion du groupe actif
  readonly prochaineReunion = computed<Reunion | undefined>(() => {
    const gid = this.groupeActif().id;
    return this.reunions().find(r => r.groupeId === gid && (r.statut === 'EN_COURS' || r.statut === 'PLANIFIEE'));
  });

  // Tour de pot en cours pour le groupe actif
  readonly potEnCours = computed<TourPot | undefined>(() => {
    const gid = this.groupeActif().id;
    return this.toursPot().find(t => t.groupeId === gid && t.statut === 'PRET_POUR_VERSEMENT') 
      || this.toursPot().find(t => t.groupeId === gid && t.statut === 'A_VENIR');
  });

  // Tour de pot de l'utilisateur connecté
  readonly monTourPot = computed<TourPot | undefined>(() => {
    const uid = this.authService.utilisateur().id;
    const gid = this.groupeActif().id;
    return this.toursPot().find(t => t.beneficiaireMembreId === uid && t.groupeId === gid);
  });

  // ACTIONS RÉACTIVES (aucune Promise)

  // Enregistrer un paiement (Cash avec preuve obligatoire ou Mobile Money)
  enregistrerPaiement(req: EnregistrerPaiementRequest): void {
    const nouveauPaiement: Paiement = {
      id: 'pay-' + Date.now(),
      cotisationId: undefined,
      groupeId: req.groupeId,
      sessionId: req.sessionId,
      membreId: req.membreId,
      membreNom: this.membres().find(m => m.id === req.membreId)?.nom || 'Membre',
      montantXaf: req.montantXaf,
      modePaiement: req.modePaiement,
      typeCotisation: req.typeCotisation,
      statut: 'VALIDE',
      cleIdempotence: req.cleIdempotence || 'idemp-' + Date.now(),
      referenceTransaction: req.referenceTransaction,
      preuveUrl: req.preuveRecuBase64 || 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?fit=crop&w=600&h=400&q=80',
      commentaire: req.commentaire,
      valideParTresorierId: this.authService.utilisateur().id,
      datePaiement: new Date().toISOString().replace('T', ' ').substring(0, 16),
      dateValidation: new Date().toISOString().replace('T', ' ').substring(0, 16),
    };

    // Mettre à jour la liste des paiements
    this.paiements.update(list => [nouveauPaiement, ...list]);

    // Mettre à jour la cotisation correspondante si existante
    this.cotisations.update(list => list.map(c => {
      if (c.groupeId === req.groupeId && c.membreId === req.membreId && c.typeCotisation === req.typeCotisation) {
        return {
          ...c,
          montantPayeXaf: c.montantPayeXaf + req.montantXaf,
          statut: 'PAYE',
          datePaiement: new Date().toISOString().substring(0, 10),
        };
      }
      return c;
    }));

    // Mettre à jour la caisse du groupe
    this.groupes.update(list => list.map(g => {
      if (g.id === req.groupeId) {
        return {
          ...g,
          soldeCaisseCashXaf: req.modePaiement === 'CASH' 
            ? g.soldeCaisseCashXaf + req.montantXaf 
            : g.soldeCaisseCashXaf,
          soldeCaisseMobileMoneyXaf: req.modePaiement !== 'CASH' 
            ? g.soldeCaisseMobileMoneyXaf + req.montantXaf 
            : g.soldeCaisseMobileMoneyXaf,
        };
      }
      return g;
    }));
  }

  // Appliquer une sanction / amende
  appliquerSanction(param: { membreId: string; typeInfraction: TypeInfraction; montantXaf: number; motif: string }): void {
    const membre = this.membres().find(m => m.id === param.membreId);
    const grp = this.groupeActif();
    const ses = this.sessionActive();

    const nouvellePenalite: Penalite = {
      id: 'pen-' + Date.now(),
      groupeId: grp.id,
      sessionId: ses?.id || 'ses-1',
      membreId: param.membreId,
      membreNom: membre ? `${membre.prenom} ${membre.nom}` : 'Membre',
      typeInfraction: param.typeInfraction,
      motif: param.motif,
      montantXaf: param.montantXaf,
      statut: 'EN_ATTENTE',
      dateApplication: new Date().toISOString().replace('T', ' ').substring(0, 16),
      appliqueParNom: `${this.authService.utilisateur().prenom} ${this.authService.utilisateur().nom} (${this.roleActif()})`,
    };

    this.penalites.update(list => [nouvellePenalite, ...list]);
  }

  // Régler une amende
  reglerSanction(penaliteId: string): void {
    this.penalites.update(list => list.map(p => {
      if (p.id === penaliteId) {
        return {
          ...p,
          statut: 'PAYEE',
          dateReglement: new Date().toISOString().replace('T', ' ').substring(0, 16),
        };
      }
      return p;
    }));
  }

  // Marquer une présence pendant la réunion en direct
  mettreAJourPresence(reunionId: string, membreId: string, nouveauStatut: StatutPresence): void {
    this.reunions.update(list => list.map(r => {
      if (r.id === reunionId) {
        return {
          ...r,
          presences: r.presences.map(p => {
            if (p.membreId === membreId) {
              return {
                ...p,
                statut: nouveauStatut,
                heureArrivee: (nouveauStatut === 'PRESENT' || nouveauStatut === 'RETARD') 
                  ? new Date().toTimeString().substring(0, 5) 
                  : undefined,
              };
            }
            return p;
          })
        };
      }
      return r;
    }));
  }

  // Sauvegarder le compte-rendu de réunion
  sauvegarderCompteRendu(reunionId: string, texte: string, cloturer: boolean): void {
    this.reunions.update(list => list.map(r => {
      if (r.id === reunionId) {
        return {
          ...r,
          compteRenduRedige: texte,
          statut: cloturer ? 'CLOTUREE' : r.statut,
          heureFin: cloturer ? new Date().toTimeString().substring(0, 5) : r.heureFin,
        };
      }
      return r;
    }));
  }

  // Décaisser le pot au bénéficiaire du tour
  decaisserPot(tourPotId: string, mode: string): void {
    this.toursPot.update(list => list.map(t => {
      if (t.id === tourPotId) {
        return {
          ...t,
          statut: 'VERSE',
          montantReelVerseXaf: t.montantTheoriqueXaf,
          dateVersementEffective: new Date().toISOString().substring(0, 10),
          modeVersement: mode,
        };
      }
      return t;
    }));
  }

  // Créer un nouveau groupe
  creerGroupe(data: {
    nom: string;
    description: string;
    typeSiege: TypeSiege;
    adresseSiegeFixe?: string;
    frequenceReunion: FrequenceReunion;
    nombreMembresMax: number;
    montantPotTourXaf: number;
  }): string {
    const nouveauId = 'grp-' + Date.now();
    const createur = this.authService.utilisateur();

    const nouveauGroupe: Groupe = {
      id: nouveauId,
      nom: data.nom,
      description: data.description,
      createurMembreId: createur.id,
      typeSiege: data.typeSiege,
      adresseSiegeFixe: data.adresseSiegeFixe,
      frequenceReunion: data.frequenceReunion,
      devise: 'XAF',
      nombreMembresMax: data.nombreMembresMax,
      nombreMembresActuels: 1,
      statut: 'EN_CREATION',
      premierBureauElu: false, // Tant que pas de bureau, le créateur a les pouvoirs techniques !
      soldeCaisseCashXaf: 0,
      soldeCaisseMobileMoneyXaf: 0,
      dateCreation: new Date().toISOString().substring(0, 10),
    };

    this.groupes.update(list => [...list, nouveauGroupe]);

    // Ajouter l'adhésion en tant que CREATEUR
    this.authService.session.update(s => ({
      ...s,
      adhesions: [
        ...s.adhesions,
        {
          id: 'adh-' + Date.now(),
          groupeId: nouveauId,
          groupeNom: data.nom,
          membreId: createur.id,
          role: 'CREATEUR',
          statut: 'ACTIF',
          totalCotiseXaf: 0,
          totalPenalitesXaf: 0,
          aRecuPot: false,
          dateAdhesion: new Date().toISOString().substring(0, 10),
        }
      ],
      groupeActifId: nouveauId,
    }));

    return nouveauId;
  }

  // Élection du premier bureau (entraîne la rétrogradation automatique du créateur)
  elireBureau(groupeId: string, presidentId: string, tresorierId: string, secretaireId: string): void {
    const pres = this.membres().find(m => m.id === presidentId);
    const tres = this.membres().find(m => m.id === tresorierId);
    const sec = this.membres().find(m => m.id === secretaireId);

    const nouveauMandat: MandatBureau = {
      id: 'man-' + Date.now(),
      groupeId,
      presidentMembreId: presidentId,
      presidentNom: pres ? `${pres.prenom} ${pres.nom}` : 'Président',
      tresorierMembreId: tresorierId,
      tresorierNom: tres ? `${tres.prenom} ${tres.nom}` : 'Trésorier',
      secretaireMembreId: secretaireId,
      secretaireNom: sec ? `${sec.prenom} ${sec.nom}` : 'Secrétaire',
      dateDebut: new Date().toISOString().substring(0, 10),
      dateFinPrevue: new Date(Date.now() + 365*24*60*60*1000).toISOString().substring(0, 10),
      estActif: true,
    };

    this.groupes.update(list => list.map(g => {
      if (g.id === groupeId) {
        return {
          ...g,
          premierBureauElu: true, // Rétrograde automatiquement le créateur
          mandatActif: nouveauMandat,
          statut: 'ACTIF',
        };
      }
      return g;
    }));

    // Si l'utilisateur connecté était le créateur, il est rétrogradé en membre ou prend le rôle assigné
    const uid = this.authService.utilisateur().id;
    let nouveauRolePourMoi: RoleMembre = 'MEMBRE';
    if (uid === presidentId) nouveauRolePourMoi = 'PRESIDENT';
    else if (uid === tresorierId) nouveauRolePourMoi = 'TRESORIER';
    else if (uid === secretaireId) nouveauRolePourMoi = 'SECRETAIRE';

    this.authService.changerRoleActifDansGroupe(groupeId, nouveauRolePourMoi);
  }
}
