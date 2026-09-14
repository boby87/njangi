import { RoleMembre } from './membre.model';

export type TypeSiege = 'FIXE' | 'ROTATIF';
export type FrequenceReunion = 'HEBDOMADAIRE' | 'BIMENSUELLE' | 'MENSUELLE' | 'PERSONNALISEE';
export type StatutGroupe = 'ACTIF' | 'EN_CREATION' | 'CLOTURE' | 'SUSPENDU';

export interface MandatBureau {
  id: string;
  groupeId: string;
  presidentMembreId?: string;
  presidentId?: string;
  presidentNom?: string;
  tresorierMembreId?: string;
  tresorierId?: string;
  tresorierNom?: string;
  secretaireMembreId?: string;
  secretaireId?: string;
  secretaireNom?: string;
  commissaireAuxComptesId?: string;
  dateDebut: string;
  dateFinPrevue: string;
  dateFinReelle?: string;
  estActif?: boolean;
  statut?: string;
}

export interface SessionTontine {
  id: string;
  groupeId: string;
  numeroSession?: number;
  libelle?: string;
  anneeOuLabel?: string;
  dateDebut: string;
  dateFinPrevue?: string;
  dateFin?: string;
  dateFinReelle?: string;
  montantPotParTourXaf?: number;
  montantPotMensuelXaf?: number;
  montantSecoursXaf?: number;
  montantReserveXaf?: number;
  estActive?: boolean;
  statut?: string;
  nombreToursPrevu?: number;
  tourActuel?: number;
  totalCollecteXaf?: number;
  totalDistribueXaf?: number;
}

export interface Groupe {
  id: string;
  nom: string;
  description: string;
  createurMembreId?: string;
  typeSiege: TypeSiege;
  adresseSiegeFixe?: string;
  ordreRotationSiege?: string[];
  frequenceReunion?: FrequenceReunion;
  frequence?: string;
  jourSeance?: string;
  reglementInterieurUrl?: string;
  devise: 'XAF';
  nombreMembresMax?: number;
  nombreMembresActuels?: number;
  montantCotisationBaseXaf?: number;
  statut: StatutGroupe;
  premierBureauElu?: boolean;
  mandatActif?: MandatBureau;
  sessionActive?: SessionTontine;
  soldeCaisseCashXaf?: number;
  soldeCaisseMobileMoneyXaf?: number;
  dateCreation: string;
}

export type GroupeTontine = Groupe;
