import { RoleMembre } from './membre.model';

export type TypeSiege = 'FIXE' | 'ROTATIF';
export type FrequenceReunion = 'HEBDOMADAIRE' | 'BIMENSUELLE' | 'MENSUELLE' | 'PERSONNALISEE';
export type StatutGroupe = 'ACTIF' | 'EN_CREATION' | 'CLOTURE' | 'SUSPENDU';

export interface MandatBureau {
  id: string;
  groupeId: string;
  presidentMembreId: string;
  presidentNom: string;
  tresorierMembreId: string;
  tresorierNom: string;
  secretaireMembreId: string;
  secretaireNom: string;
  dateDebut: string;
  dateFinPrevue: string;
  dateFinReelle?: string;
  estActif: boolean;
}

export interface SessionTontine {
  id: string;
  groupeId: string;
  numeroSession: number;
  libelle: string; // ex: "Session 2026-2027"
  dateDebut: string;
  dateFinPrevue: string;
  dateFinReelle?: string;
  montantPotParTourXaf: number;
  montantSecoursXaf: number;
  montantReserveXaf: number;
  estActive: boolean;
  totalCollecteXaf: number;
  totalDistribueXaf: number;
}

export interface Groupe {
  id: string;
  nom: string;
  description: string;
  createurMembreId: string;
  typeSiege: TypeSiege;
  adresseSiegeFixe?: string;
  ordreRotationSiege?: string[]; // IDs des membres hôtes
  frequenceReunion: FrequenceReunion;
  devise: 'XAF';
  nombreMembresMax: number;
  nombreMembresActuels: number;
  statut: StatutGroupe;
  premierBureauElu: boolean; // Si true, le créateur est rétrogradé en membre simple
  mandatActif?: MandatBureau;
  sessionActive?: SessionTontine;
  soldeCaisseCashXaf: number;
  soldeCaisseMobileMoneyXaf: number;
  dateCreation: string;
}
