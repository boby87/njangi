export type TypeCotisation = 
  | 'ROTATIVE_POT'
  | 'SECOURS_DECES'
  | 'CAISSE_RESERVE'
  | 'EVENEMENTIELLE';

export type StatutCotisation = 
  | 'EN_ATTENTE'
  | 'PAYE'
  | 'EN_RETARD'
  | 'PENALISE';

export type StatutPot = 
  | 'A_VENIR'
  | 'PRET_POUR_VERSEMENT'
  | 'VERSE'
  | 'REPORTE';

export interface Cotisation {
  id: string;
  groupeId: string;
  sessionId: string;
  reunionId?: string;
  membreId: string;
  membreNom: string;
  typeCotisation: TypeCotisation;
  montantAttenduXaf: number;
  montantPayeXaf: number;
  dateEcheance: string;
  datePaiement?: string;
  statut: StatutCotisation;
  penaliteAppliqueeXaf?: number;
}

export interface TourPot {
  id: string;
  groupeId: string;
  sessionId: string;
  reunionId?: string;
  numeroTour: number;
  beneficiaireMembreId: string;
  beneficiaireNom: string;
  montantTheoriqueXaf: number;
  montantReelVerseXaf: number;
  dateAttribution: string;
  dateVersementEffective?: string;
  statut: StatutPot;
  modeVersement?: string;
  recuSigneUrl?: string;
  observations?: string;
}
