import { TypeSiege } from './groupe.model';

export type StatutPresence = 'PRESENT' | 'RETARD' | 'ABSENT_JUSTIFIE' | 'ABSENT_NON_JUSTIFIE' | 'EXCUSE' | 'ABSENT';
export type StatutReunion = 'PLANIFIEE' | 'EN_COURS' | 'CLOTUREE' | 'ANNULEE';

export interface PresenceSeance {
  id?: string;
  reunionId?: string;
  membreId: string;
  membreNom?: string;
  statut: StatutPresence;
  heureArrivee?: string;
  motifAbsence?: string;
  sanctionAppliquee?: boolean;
}

export type PresenceMembre = PresenceSeance;

export interface Reunion {
  id: string;
  groupeId: string;
  sessionId: string;
  numeroOrdre?: number;
  titre?: string;
  dateReunion?: string;
  dateHeureDebut?: string;
  heureDebut?: string;
  heureFin?: string;
  typeSiege?: TypeSiege;
  presidentSeanceId?: string;
  secretaireSeanceId?: string;
  hoteId?: string;
  hoteMembreId?: string;
  hoteNom?: string;
  lieu?: string;
  lieuAdresse?: string;
  ordreDuJour: string[];
  presences?: PresenceSeance[];
  totalCollecteSeanceXaf?: number;
  totalAmendesSeanceXaf?: number;
  compteRenduRedige?: string;
  beneficiairePotDuJourId?: string;
  statut: StatutReunion;
}
