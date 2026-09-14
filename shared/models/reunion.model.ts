import { TypeSiege } from './groupe.model';

export type StatutPresence = 'PRESENT' | 'RETARD' | 'ABSENT_JUSTIFIE' | 'ABSENT_NON_JUSTIFIE';
export type StatutReunion = 'PLANIFIEE' | 'EN_COURS' | 'CLOTUREE' | 'ANNULEE';

export interface PresenceMembre {
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
  hoteMembreId?: string;
  hoteNom?: string;
  lieuAdresse: string;
  ordreDuJour: string[];
  presences: PresenceMembre[];
  totalCollecteSeanceXaf: number;
  totalAmendesSeanceXaf: number;
  compteRenduRedige?: string;
  statut: StatutReunion;
}
