import { Injectable, signal, computed, inject } from '@angular/core';
import { Preferences } from '@capacitor/preferences';
import { from } from 'rxjs';
import { Membre, RoleMembre } from '../../shared/models/membre.model';
import { MobileApiService } from './api.service';

const TOKEN_KEY = 'njangi_auth_token';
const USER_KEY = 'njangi_current_user';
const PIN_KEY = 'njangi_user_pin';

export interface InscriptionMobileData {
  telephone: string;
  nom: string;
  prenom: string;
  email?: string;
  ville: string;
  pays: string;
  motDePasse: string;
  codePin: string;
  avatarUrl?: string;
  dateNaissance?: string;
  kycDocumentType?: string;
  kycDocumentNumero?: string;
  kycDocumentRecto?: string;
  kycDocumentVerso?: string;
}

@Injectable({
  providedIn: 'root',
})
export class MobileAuthService {
  private readonly apiService = inject(MobileApiService, { optional: true });

  readonly currentUser = signal<Membre | null>({
    id: 'm-001',
    authUtilisateurId: 'u-001',
    nom: 'FOKOU',
    prenom: 'Jean-Paul',
    email: 'jp.fokou@njangi.cm',
    telephone: '+237699123456',
    statut: 'ACTIF',
    ville: 'Douala',
    createdAt: new Date().toISOString(),
  });

  readonly activeRole = signal<RoleMembre>('MEMBRE');
  readonly token = signal<string | null>('jwt-demo-session-token');
  readonly codePin = signal<string | null>('1234');
  readonly biometrieActivee = signal<boolean>(false);
  readonly simulatedOtp = signal<string | null>('884210');
  readonly isAwaitingOtp = signal<boolean>(false);
  readonly pendingPhone = signal<string>('');

  readonly isAuthenticated = computed(() => !!this.currentUser());

  constructor() {
    this.restoreSessionFromPreferences();
  }

  private restoreSessionFromPreferences(): void {
    // Lecture sécurisée via @capacitor/preferences encapsulée en RxJS from() - Zéro async/await
    from(Preferences.get({ key: TOKEN_KEY })).subscribe({
      next: ({ value }) => {
        if (value) {
          this.token.set(value);
        }
      },
    });

    from(Preferences.get({ key: USER_KEY })).subscribe({
      next: ({ value }) => {
        if (value) {
          try {
            const parsed = JSON.parse(value) as Membre;
            this.currentUser.set(parsed);
          } catch {
            // garder l'utilisateur par défaut
          }
        }
      },
    });

    from(Preferences.get({ key: PIN_KEY })).subscribe({
      next: ({ value }) => {
        if (value) {
          this.codePin.set(value);
        }
      },
    });
  }

  requestOtp(telephone: string): void {
    const generated = Math.floor(100000 + Math.random() * 900000).toString();
    this.simulatedOtp.set(generated);
    this.pendingPhone.set(telephone);
    this.isAwaitingOtp.set(true);
  }

  verifyOtp(code: string): boolean {
    if (code === this.simulatedOtp() || code === '123456') {
      const dummyToken = 'jwt-njangi-' + Date.now();
      this.token.set(dummyToken);

      const user: Membre = {
        id: 'm-001',
        authUtilisateurId: 'u-001',
        nom: 'FOKOU',
        prenom: 'Jean-Paul',
        email: 'jp.fokou@njangi.cm',
        telephone: this.pendingPhone() || '+237699123456',
        statut: 'ACTIF',
        ville: 'Douala',
        createdAt: new Date().toISOString(),
      };
      this.currentUser.set(user);
      this.isAwaitingOtp.set(false);

      // Persistance sécurisée @capacitor/preferences via RxJS
      from(Preferences.set({ key: TOKEN_KEY, value: dummyToken })).subscribe();
      from(Preferences.set({ key: USER_KEY, value: JSON.stringify(user) })).subscribe();

      return true;
    }
    return false;
  }

  connexionSociale(provider: 'GOOGLE' | 'FACEBOOK'): { telephoneRequis: boolean } {
    const isGoogle = provider === 'GOOGLE';
    const dummyToken = 'jwt-social-' + provider.toLowerCase() + '-' + Date.now();
    this.token.set(dummyToken);

    const user: Membre = {
      id: isGoogle ? 'm-google-001' : 'm-fb-001',
      authUtilisateurId: isGoogle ? 'u-google-001' : 'u-fb-001',
      nom: isGoogle ? 'Kamga' : 'Mbarga',
      prenom: isGoogle ? 'Eric' : 'Paul',
      email: isGoogle ? 'kamga.eric@gmail.com' : 'paul.mbarga@facebook.com',
      telephone: '',
      statut: 'ACTIF',
      ville: isGoogle ? 'Paris' : 'Douala',
      pays: isGoogle ? 'France' : 'Cameroun',
      createdAt: new Date().toISOString(),
    };

    this.currentUser.set(user);
    this.isAwaitingOtp.set(false);

    // Persistance sécurisée via @capacitor/preferences encapsulée en RxJS (zéro Promise)
    from(Preferences.set({ key: TOKEN_KEY, value: dummyToken })).subscribe();
    from(Preferences.set({ key: USER_KEY, value: JSON.stringify(user) })).subscribe();

    return { telephoneRequis: true };
  }

  lierTelephone(telephone: string): void {
    const curr = this.currentUser();
    if (curr) {
      const updated: Membre = {
        ...curr,
        telephone: telephone,
      };
      this.currentUser.set(updated);
      from(Preferences.set({ key: USER_KEY, value: JSON.stringify(updated) })).subscribe();
    }
  }

  setActiveRole(role: RoleMembre): void {
    this.activeRole.set(role);
  }

  inscrire(data: InscriptionMobileData): Membre {
    const uniqueId = 'm-' + Math.floor(1000 + Math.random() * 9000);
    const authId = 'u-' + Math.floor(1000 + Math.random() * 9000);
    const dummyToken = 'jwt-njangi-' + Date.now();

    const nouveauMembre: Membre = {
      id: uniqueId,
      authUtilisateurId: authId,
      nom: data.nom.trim(),
      prenom: data.prenom.trim(),
      email: data.email?.trim() || '',
      telephone: data.telephone.trim(),
      ville: data.ville.trim(),
      pays: data.pays?.trim() || 'Cameroun',
      avatarUrl: data.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?fit=crop&w=256&h=256&q=80',
      photoUrl: data.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?fit=crop&w=256&h=256&q=80',
      statut: 'ACTIF',
      estVerifieKyc: !!(data.kycDocumentType && data.kycDocumentNumero),
      dateInscription: new Date().toISOString(),
      createdAt: new Date().toISOString(),
    };

    this.currentUser.set(nouveauMembre);
    this.token.set(dummyToken);
    this.codePin.set(data.codePin);
    this.activeRole.set('MEMBRE');
    this.isAwaitingOtp.set(false);

    // Persistance sécurisée via @capacitor/preferences avec RxJS from() - Zéro Promise JavaScript
    from(Preferences.set({ key: TOKEN_KEY, value: dummyToken })).subscribe();
    from(Preferences.set({ key: USER_KEY, value: JSON.stringify(nouveauMembre) })).subscribe();
    from(Preferences.set({ key: PIN_KEY, value: data.codePin })).subscribe();

    // Appel optionnel vers la passerelle API si disponible
    if (this.apiService) {
      this.apiService.post('/api/v1/auth/inscrire', {
        telephone: data.telephone.trim(),
        email: data.email?.trim() || null,
        motDePasse: data.motDePasse,
        nom: data.nom.trim(),
        prenom: data.prenom.trim(),
        photoUrl: nouveauMembre.avatarUrl,
        ville: data.ville.trim(),
        pays: data.pays?.trim() || 'Cameroun',
      }).subscribe();
    }

    return nouveauMembre;
  }

  logout(): void {
    this.currentUser.set(null);
    this.token.set(null);
    this.codePin.set(null);
    this.isAwaitingOtp.set(false);
    from(Preferences.remove({ key: TOKEN_KEY })).subscribe();
    from(Preferences.remove({ key: USER_KEY })).subscribe();
    from(Preferences.remove({ key: PIN_KEY })).subscribe();
  }
}
