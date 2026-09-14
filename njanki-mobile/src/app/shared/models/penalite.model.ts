export type TypeInfraction = 
  | 'RETARD_PAIEMENT'
  | 'ABSENCE_NON_JUSTIFIEE'
  | 'RETARD_REUNION'
  | 'BAVARDAGE_SONNERIE'
  | 'NON_RESPECT_REGLE'
  | 'AUTRE';

export type StatutPenalite = 'EN_ATTENTE' | 'PAYEE' | 'ANNULEE' | 'PAYE' | 'NON_PAYE';

export interface TarificationPenalite {
  id?: string;
  groupeId?: string;
  typeInfraction: TypeInfraction;
  libelle?: string;
  intitule?: string;
  montantForfaitaireXaf?: number;
  montantParDefautXaf?: number;
  estPourcentage?: boolean;
  valeurPourcentage?: number;
  actif?: boolean;
}

export type BaremePenalite = TarificationPenalite;

export interface Penalite {
  id: string;
  groupeId: string;
  sessionId?: string;
  reunionId?: string;
  membreId: string;
  membreNom?: string;
  typeInfraction: TypeInfraction;
  motif: string;
  montantXaf: number;
  statut: StatutPenalite;
  dateApplication?: string;
  dateSanction?: string;
  dateReglement?: string;
  appliqueParNom?: string;
}

export type Sanction = Penalite;
