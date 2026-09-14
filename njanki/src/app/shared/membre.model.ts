export type RoleMembre = 
  | 'CREATEUR'
  | 'PRESIDENT'
  | 'TRESORIER'
  | 'SECRETAIRE'
  | 'MEMBRE'
  | 'AUDITEUR';

export type StatutMembre = 'ACTIF' | 'EN_ATTENTE' | 'SUSPENDU' | 'RADIE';

export interface Membre {
  id: string;
  nom: string;
  prenom: string;
  telephone: string; // Format: +237XXXXXXXXX
  email: string;
  avatarUrl?: string;
  ville: string;
  pays: string;
  dateInscription: string;
  estVerifieKyc: boolean;
}

export interface AdhesionGroupe {
  id: string;
  groupeId: string;
  groupeNom: string;
  membreId: string;
  role: RoleMembre;
  statut: StatutMembre;
  ordrePassagePot?: number;
  totalCotiseXaf: number;
  totalPenalitesXaf: number;
  aRecuPot: boolean;
  dateAdhesion: string;
}

export interface UtilisateurSession {
  membre: Membre;
  token: string;
  adhesions: AdhesionGroupe[];
  groupeActifId?: string;
}
