export type RoleMembre = 
  | 'CREATEUR'
  | 'PRESIDENT'
  | 'TRESORIER'
  | 'SECRETAIRE'
  | 'MEMBRE'
  | 'AUDITEUR';

export type StatutMembre = 'ACTIF' | 'EN_ATTENTE' | 'SUSPENDU' | 'RADIE' | 'INACTIF';

export interface Membre {
  id: string;
  authUtilisateurId?: string;
  nom: string;
  prenom: string;
  telephone: string; // Format: +237XXXXXXXXX
  email: string;
  avatarUrl?: string;
  photoUrl?: string;
  ville: string;
  pays?: string;
  statut?: StatutMembre;
  dateInscription?: string;
  createdAt?: string;
  estVerifieKyc?: boolean;
}

export interface AdhesionGroupe {
  id: string;
  groupeId: string;
  groupeNom?: string;
  membreId?: string;
  utilisateurId?: string;
  role: RoleMembre;
  statut: StatutMembre;
  ordrePassagePot?: number;
  totalCotiseXaf?: number;
  totalPenalitesXaf?: number;
  aRecuPot?: boolean;
  dateAdhesion?: string;
  rejointLe?: string;
}

export interface UtilisateurSession {
  membre: Membre;
  token: string;
  adhesions: AdhesionGroupe[];
  groupeActifId?: string;
}
