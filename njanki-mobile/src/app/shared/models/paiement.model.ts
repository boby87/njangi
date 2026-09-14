import { TypeCotisation } from './cotisation.model';

export type ModePaiement = 'CASH' | 'ORANGE_MONEY' | 'MTN_MOMO' | 'CARTE';
export type StatutPaiement = 'INITIE' | 'VALIDE' | 'REJETE' | 'ANNULE';

export interface Paiement {
  id: string;
  cotisationId?: string;
  groupeId: string;
  sessionId: string;
  membreId: string;
  membreNom?: string;
  montantXaf: number;
  modePaiement: ModePaiement;
  typeCotisation?: TypeCotisation;
  statut: StatutPaiement;
  cleIdempotence?: string;
  idempotencyKey?: string;
  referenceTransaction?: string;
  transactionIdOperateur?: string;
  preuveUrl?: string; // Image du reçu signé obligatoire pour Cash
  recuPhysiqueSigneUrl?: string;
  commentaire?: string;
  valideParTresorierId?: string;
  tresorierValidateurId?: string;
  datePaiement?: string;
  dateValidation?: string;
}

export interface EnregistrerPaiementRequest {
  groupeId: string;
  sessionId: string;
  membreId: string;
  typeCotisation: TypeCotisation;
  montantXaf: number;
  modePaiement: ModePaiement;
  referenceTransaction?: string;
  preuveRecuBase64?: string;
  cleIdempotence: string;
  commentaire?: string;
}
