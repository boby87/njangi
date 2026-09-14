import { Injectable, signal, computed } from '@angular/core';
import { Membre, UtilisateurSession, AdhesionGroupe } from '@shared/models/membre.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Utilisateur connecté par défaut (profil test réaliste camerounais)
  private readonly initialMembre: Membre = {
    id: 'usr-1',
    nom: 'Mbarga',
    prenom: 'Jean-Paul',
    telephone: '+237699123456',
    email: 'jp.mbarga@njangi.cm',
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?fit=crop&w=256&h=256&q=80',
    ville: 'Douala',
    pays: 'Cameroun',
    dateInscription: '2025-01-15',
    estVerifieKyc: true,
  };

  private readonly initialAdhesions: AdhesionGroupe[] = [
    {
      id: 'adh-1',
      groupeId: 'grp-1',
      groupeNom: 'Solidarité Douala Akwa',
      membreId: 'usr-1',
      role: 'PRESIDENT',
      statut: 'ACTIF',
      ordrePassagePot: 1,
      totalCotiseXaf: 350000,
      totalPenalitesXaf: 0,
      aRecuPot: false,
      dateAdhesion: '2025-02-01',
    },
    {
      id: 'adh-2',
      groupeId: 'grp-2',
      groupeNom: 'Njangi Diaspora Yaoundé',
      membreId: 'usr-1',
      role: 'MEMBRE',
      statut: 'ACTIF',
      ordrePassagePot: 5,
      totalCotiseXaf: 150000,
      totalPenalitesXaf: 1000,
      aRecuPot: true,
      dateAdhesion: '2025-03-10',
    },
    {
      id: 'adh-3',
      groupeId: 'grp-3',
      groupeNom: 'Tontine Jeunes Entrepreneurs',
      membreId: 'usr-1',
      role: 'CREATEUR',
      statut: 'ACTIF',
      ordrePassagePot: 2,
      totalCotiseXaf: 50000,
      totalPenalitesXaf: 0,
      aRecuPot: false,
      dateAdhesion: '2026-01-05',
    }
  ];

  // État réactif avec Signals purs
  readonly session = signal<UtilisateurSession>({
    membre: this.initialMembre,
    token: 'jwt_mock_token_njangi_valid',
    adhesions: this.initialAdhesions,
    groupeActifId: 'grp-1',
  });

  readonly utilisateur = computed(() => this.session().membre);
  readonly estConnecte = computed(() => !!this.session().token);
  readonly adhesions = computed(() => this.session().adhesions);
  readonly groupeActifId = computed(() => this.session().groupeActifId || 'grp-1');

  // Simulation OTP réactive
  readonly otpEnvoye = signal<boolean>(false);
  readonly dernierIdentifiant = signal<string>('');

  demanderOtp(identifiant: string): void {
    this.dernierIdentifiant.set(identifiant);
    this.otpEnvoye.set(true);
  }

  verifierOtp(code: string): boolean {
    if (code === '123456' || code.length === 6) {
      this.session.update(s => ({
        ...s,
        token: 'jwt_mock_token_valid_' + Date.now(),
      }));
      this.otpEnvoye.set(false);
      return true;
    }
    return false;
  }

  deconnecter(): void {
    this.session.update(s => ({
      ...s,
      token: '',
    }));
  }

  changerRoleActifDansGroupe(groupeId: string, nouveauRole: import('@shared/models/membre.model').RoleMembre): void {
    this.session.update(s => ({
      ...s,
      adhesions: s.adhesions.map(a => a.groupeId === groupeId ? { ...a, role: nouveauRole } : a)
    }));
  }

  setGroupeActif(groupeId: string): void {
    this.session.update(s => ({
      ...s,
      groupeActifId: groupeId,
    }));
  }

  connexionSociale(provider: 'GOOGLE' | 'FACEBOOK'): { telephoneRequis: boolean } {
    const isGoogle = provider === 'GOOGLE';
    const socialMembre: Membre = {
      id: isGoogle ? 'usr-google-1' : 'usr-fb-1',
      nom: isGoogle ? 'Kamga' : 'Mbarga',
      prenom: isGoogle ? 'Eric' : 'Paul',
      telephone: '',
      email: isGoogle ? 'kamga.eric@gmail.com' : 'paul.mbarga@facebook.com',
      avatarUrl: isGoogle
        ? 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?fit=crop&w=256&h=256&q=80'
        : 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?fit=crop&w=256&h=256&q=80',
      ville: isGoogle ? 'Paris' : 'Douala',
      pays: isGoogle ? 'France' : 'Cameroun',
      dateInscription: '2026-03-14',
      estVerifieKyc: true,
    };

    this.session.update(s => ({
      ...s,
      membre: socialMembre,
      token: 'jwt_social_' + provider.toLowerCase() + '_' + Date.now(),
    }));

    return { telephoneRequis: true };
  }

  lierTelephone(telephone: string): void {
    this.session.update(s => ({
      ...s,
      membre: {
        ...s.membre,
        telephone: telephone,
      }
    }));
  }
}
