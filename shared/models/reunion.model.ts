import { TypeSiege } from './groupe.model';

export type StatutPresence =
  | 'PRESENT'
  | 'RETARD'
  | 'EXCUSE'
  | 'ABSENT'
  | 'ABSENT_JUSTIFIE'
  | 'ABSENT_NON_JUSTIFIE';

export type StatutReunion = 'PLANIFIEE' | 'EN_COURS' | 'CLOTUREE' | 'TERMINEE' | 'ANNULEE';

export interface PresenceMembre {
  id?: string;
  reunionId?: string;
  membreId: string;
  membreNom: string;
  statut: StatutPresence;
  heureArrivee?: string;
  motifAbsence?: string;
  sanctionAppliquee?: boolean;
}

export interface Reunion {
  id: string;
  groupeId: string;
  sessionId: string;
  numeroOrdre: number;
  titre: string;
  dateReunion: string;
  heureDebut: string;
  heureFin?: string;
  typeSiege: TypeSiege;
  presidentSeanceId?: string;
  secretaireSeanceId?: string;
  hoteMembreId?: string;
  hoteNom?: string;
  lieuAdresse: string;
  ordreDuJour: string[];
  presences: PresenceMembre[];
  beneficiairePotDuJourId?: string;
  totalCollecteSeanceXaf: number;
  totalAmendesSeanceXaf: number;
  compteRenduRedige?: string;
  statut: StatutReunion;
}
