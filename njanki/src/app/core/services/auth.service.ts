import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import { ApiResponse } from '@shared/models/api-response.model';
import {
  AdhesionGroupe,
  Membre,
  RoleMembre,
  StatutMembre,
  UtilisateurSession,
} from '@shared/models/membre.model';
import { environment } from '../../../environments/environment';

const SESSION_STORAGE_KEY = 'njangi_web_session';
const DEMO_TOKEN = 'njangi-demo-session';
const DEFAULT_AVATAR_URL =
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?fit=crop&w=256&h=256&q=80';

const DEMO_MEMBRE: Membre = {
  id: 'usr-1',
  nom: 'Mbarga',
  prenom: 'Jean-Paul',
  telephone: '+237699123456',
  email: 'jp.mbarga@njangi.cm',
  avatarUrl: DEFAULT_AVATAR_URL,
  ville: 'Douala',
  pays: 'Cameroun',
  dateInscription: '2025-01-15',
  estVerifieKyc: true,
};

const DEMO_ADHESIONS: AdhesionGroupe[] = [
  {
    id: 'adh-1',
    groupeId: 'grp-1',
    groupeNom: 'Solidarité Douala Akwa',
    membreId: 'usr-1',
    utilisateurId: 'usr-1',
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
    utilisateurId: 'usr-1',
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
    utilisateurId: 'usr-1',
    role: 'CREATEUR',
    statut: 'ACTIF',
    ordrePassagePot: 2,
    totalCotiseXaf: 50000,
    totalPenalitesXaf: 0,
    aRecuPot: false,
    dateAdhesion: '2026-01-05',
  },
];

const GUEST_MEMBRE: Membre = {
  id: '',
  nom: '',
  prenom: '',
  telephone: '',
  email: '',
  avatarUrl: DEFAULT_AVATAR_URL,
  ville: '',
  pays: 'Cameroun',
  dateInscription: '',
  estVerifieKyc: false,
};

interface BackendUtilisateurDto {
  id: string;
  telephone?: string | null;
  email?: string | null;
  nom?: string | null;
  prenom?: string | null;
  photoUrl?: string | null;
  ville?: string | null;
  pays?: string | null;
  statut?: string | null;
  creeLe?: string | null;
}

interface BackendLoginResponse {
  accessToken: string;
  tokenType?: string;
  expiresIn?: number;
  utilisateur: BackendUtilisateurDto;
}

interface BackendSocialLoginResponse extends BackendLoginResponse {
  telephoneRequis: boolean;
}

interface BackendAdhesionGroupeDto {
  id: string;
  utilisateurId?: string;
  membreId?: string;
  groupeId: string;
  groupeNom?: string;
  role: RoleMembre;
  statut?: StatutMembre;
  ordrePassagePot?: number;
  totalCotiseXaf?: number;
  totalPenalitesXaf?: number;
  aRecuPot?: boolean;
  rejointLe?: string;
  dateAdhesion?: string;
}

export interface SocialLoginPayload {
  token?: string;
  providerId?: string;
  email?: string;
  nom?: string;
  prenom?: string;
  photoUrl?: string;
}

export interface SsoPublicConfig {
  googleEnabled: boolean;
  googleClientId: string;
  devFallbackEnabled: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly membresUrl = `${environment.apiUrl}/membres`;

  // État réactif avec Signals purs — non authentifié par défaut sauf restauration sessionStorage
  readonly session = signal<UtilisateurSession>(this.restaurerSession());

  readonly utilisateur = computed<Membre>(() => this.session().membre);
  readonly estConnecte = computed<boolean>(() => Boolean(this.session().token));
  readonly estModeDemo = computed<boolean>(() => this.session().token === DEMO_TOKEN);
  readonly adhesions = computed<AdhesionGroupe[]>(() => this.session().adhesions);
  readonly groupeActifId = computed<string>(
    () => this.session().groupeActifId || this.session().adhesions[0]?.groupeId || ''
  );

  // Suivi OTP réactif
  readonly otpEnvoye = signal<boolean>(false);
  readonly dernierIdentifiant = signal<string>('');

  demanderOtp(identifiant: string): Observable<ApiResponse<void>> {
    return this.http
      .post<ApiResponse<void>>(`${this.authUrl}/otp/demander`, { identifiant })
      .pipe(
        tap(() => {
          this.dernierIdentifiant.set(identifiant);
          this.otpEnvoye.set(true);
        })
      );
  }

  verifierOtp(identifiant: string, code: string): Observable<boolean> {
    return this.http
      .post<ApiResponse<BackendLoginResponse>>(`${this.authUrl}/otp/verifier`, {
        identifiant,
        code,
      })
      .pipe(
        switchMap((res) => this.finaliserAuthentification(res.data)),
        tap((succes) => {
          if (succes) {
            this.otpEnvoye.set(false);
          }
        })
      );
  }

  connexionMotDePasse(identifiant: string, motDePasse: string): Observable<boolean> {
    return this.http
      .post<ApiResponse<BackendLoginResponse>>(`${this.authUrl}/login`, {
        identifiant,
        motDePasse,
      })
      .pipe(switchMap((res) => this.finaliserAuthentification(res.data)));
  }

  chargerConfigSso(): Observable<SsoPublicConfig> {
    return this.http.get<ApiResponse<SsoPublicConfig>>(`${this.authUrl}/social/config`).pipe(
      map((res) => ({
        googleEnabled: Boolean(res.data?.googleEnabled || environment.googleClientId),
        googleClientId: environment.googleClientId || res.data?.googleClientId || '',
        devFallbackEnabled: Boolean(res.data?.devFallbackEnabled),
      })),
      catchError(() =>
        of({
          googleEnabled: Boolean(environment.googleClientId),
          googleClientId: environment.googleClientId || '',
          devFallbackEnabled: !environment.production,
        })
      )
    );
  }

  connexionGoogleAvecToken(idToken: string): Observable<{ succes: boolean; telephoneRequis: boolean }> {
    return this.http
      .post<ApiResponse<BackendSocialLoginResponse>>(`${this.authUrl}/social/login`, {
        provider: 'GOOGLE',
        token: idToken,
      })
      .pipe(
        switchMap((res) => {
          const data = res.data;
          if (!data?.accessToken) {
            return of({ succes: false, telephoneRequis: false });
          }
          const telephoneRequis = Boolean(data.telephoneRequis);
          return this.finaliserAuthentification(data).pipe(
            map((succes) => ({ succes, telephoneRequis }))
          );
        })
      );
  }

  connexionSociale(
    provider: 'GOOGLE' | 'FACEBOOK',
    payload?: SocialLoginPayload
  ): Observable<{ succes: boolean; telephoneRequis: boolean }> {
    const body = payload?.token
      ? {
          provider,
          token: payload.token,
        }
      : {
          provider,
          token: '',
          providerId: payload?.providerId || `${provider.toLowerCase()}-web-${Date.now()}`,
          email: payload?.email || '',
          nom: payload?.nom || 'Njangi',
          prenom: payload?.prenom || 'Membre',
          photoUrl: payload?.photoUrl || '',
        };

    return this.http
      .post<ApiResponse<BackendSocialLoginResponse>>(`${this.authUrl}/social/login`, body)
      .pipe(
        switchMap((res) => {
          const data = res.data;
          if (!data?.accessToken) {
            return of({ succes: false, telephoneRequis: false });
          }
          const telephoneRequis = Boolean(data.telephoneRequis);
          return this.finaliserAuthentification(data).pipe(
            map((succes) => ({ succes, telephoneRequis }))
          );
        })
      );
  }

  lierTelephone(telephone: string, codeOtp?: string): Observable<boolean> {
    const body = codeOtp ? { telephone, codeOtp } : { telephone };
    return this.http
      .post<ApiResponse<BackendLoginResponse>>(`${this.authUrl}/social/lier-telephone`, body)
      .pipe(
        map((res) => {
          const data = res.data;
          this.session.update((current) => {
            const membreMaj = data?.utilisateur
              ? this.mapperUtilisateurVersMembre(data.utilisateur)
              : { ...current.membre, telephone };
            const updated: UtilisateurSession = {
              ...current,
              token: data?.accessToken || current.token,
              membre: {
                ...membreMaj,
                telephone: membreMaj.telephone || telephone,
              },
            };
            this.sauvegarderSession(updated);
            return updated;
          });
          return true;
        })
      );
  }

  connexionDemo(): void {
    const demoSession: UtilisateurSession = {
      membre: { ...DEMO_MEMBRE },
      token: DEMO_TOKEN,
      adhesions: DEMO_ADHESIONS.map((a) => ({ ...a })),
      groupeActifId: 'grp-1',
    };
    this.session.set(demoSession);
    this.sauvegarderSession(demoSession);
    this.router.navigate(['/dashboard']);
  }

  deconnecter(): void {
    this.session.set(this.creerSessionVide());
    this.otpEnvoye.set(false);
    this.supprimerSessionStockee();
    this.router.navigate(['/login']);
  }

  changerRoleActifDansGroupe(groupeId: string, nouveauRole: RoleMembre): void {
    this.session.update((s) => {
      const updated: UtilisateurSession = {
        ...s,
        adhesions: s.adhesions.map((a) =>
          a.groupeId === groupeId ? { ...a, role: nouveauRole } : a
        ),
      };
      this.sauvegarderSession(updated);
      return updated;
    });
  }

  setGroupeActif(groupeId: string): void {
    this.session.update((s) => {
      const updated: UtilisateurSession = {
        ...s,
        groupeActifId: groupeId,
      };
      this.sauvegarderSession(updated);
      return updated;
    });
  }

  private finaliserAuthentification(data?: BackendLoginResponse | null): Observable<boolean> {
    if (!data?.accessToken || !data.utilisateur) {
      return of(false);
    }

    const membre = this.mapperUtilisateurVersMembre(data.utilisateur);
    const nouvelleSession: UtilisateurSession = {
      membre,
      token: data.accessToken,
      adhesions: [],
      groupeActifId: '',
    };

    this.session.set(nouvelleSession);
    this.sauvegarderSession(nouvelleSession);

    if (!membre.id) {
      return of(true);
    }

    return this.chargerAdhesionsUtilisateur(membre.id).pipe(
      tap((adhesions) => {
        this.session.update((current) => {
          const updated: UtilisateurSession = {
            ...current,
            adhesions,
            groupeActifId: current.groupeActifId || adhesions[0]?.groupeId || '',
          };
          this.sauvegarderSession(updated);
          return updated;
        });
      }),
      map(() => true)
    );
  }

  private chargerAdhesionsUtilisateur(utilisateurId: string): Observable<AdhesionGroupe[]> {
    return this.http
      .get<BackendAdhesionGroupeDto[] | ApiResponse<BackendAdhesionGroupeDto[]>>(
        `${this.membresUrl}/utilisateurs/${utilisateurId}/adhesions`
      )
      .pipe(
        map((response) => {
          const list = Array.isArray(response) ? response : response?.data;
          if (!Array.isArray(list)) {
            return [];
          }
          return list.map((item) => this.mapperAdhesion(item, utilisateurId));
        }),
        catchError(() => of([]))
      );
  }

  private mapperUtilisateurVersMembre(dto: BackendUtilisateurDto): Membre {
    return {
      id: dto.id || '',
      authUtilisateurId: dto.id || '',
      nom: dto.nom || '',
      prenom: dto.prenom || '',
      telephone: dto.telephone || '',
      email: dto.email || '',
      avatarUrl: dto.photoUrl || DEFAULT_AVATAR_URL,
      photoUrl: dto.photoUrl || undefined,
      ville: dto.ville || 'Douala',
      pays: dto.pays || 'Cameroun',
      dateInscription: dto.creeLe ? dto.creeLe.substring(0, 10) : '',
      createdAt: dto.creeLe || undefined,
      estVerifieKyc: dto.statut === 'ACTIF',
    };
  }

  private mapperAdhesion(dto: BackendAdhesionGroupeDto, utilisateurId: string): AdhesionGroupe {
    return {
      id: dto.id,
      groupeId: dto.groupeId,
      groupeNom: dto.groupeNom || '',
      membreId: dto.membreId || dto.utilisateurId || utilisateurId,
      utilisateurId: dto.utilisateurId || utilisateurId,
      role: dto.role || 'MEMBRE',
      statut: dto.statut || 'ACTIF',
      ordrePassagePot: dto.ordrePassagePot,
      totalCotiseXaf: dto.totalCotiseXaf ?? 0,
      totalPenalitesXaf: dto.totalPenalitesXaf ?? 0,
      aRecuPot: dto.aRecuPot ?? false,
      dateAdhesion: (dto.dateAdhesion || dto.rejointLe || '').substring(0, 10),
    };
  }

  private creerSessionVide(): UtilisateurSession {
    return {
      membre: { ...GUEST_MEMBRE },
      token: '',
      adhesions: [],
      groupeActifId: '',
    };
  }

  private restaurerSession(): UtilisateurSession {
    if (!isPlatformBrowser(this.platformId)) {
      return this.creerSessionVide();
    }

    try {
      const brut = sessionStorage.getItem(SESSION_STORAGE_KEY);
      if (!brut) {
        return this.creerSessionVide();
      }
      const parsed = JSON.parse(brut) as Partial<UtilisateurSession>;
      if (!parsed?.token || typeof parsed.token !== 'string') {
        return this.creerSessionVide();
      }
      return {
        membre: { ...GUEST_MEMBRE, ...(parsed.membre || {}) },
        token: parsed.token,
        adhesions: Array.isArray(parsed.adhesions) ? parsed.adhesions : [],
        groupeActifId: typeof parsed.groupeActifId === 'string' ? parsed.groupeActifId : '',
      };
    } catch {
      return this.creerSessionVide();
    }
  }

  private sauvegarderSession(session: UtilisateurSession): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    try {
      if (session.token) {
        sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
      } else {
        sessionStorage.removeItem(SESSION_STORAGE_KEY);
      }
    } catch {
      // Ignorer les restrictions éventuelles de sessionStorage
    }
  }

  private supprimerSessionStockee(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    try {
      sessionStorage.removeItem(SESSION_STORAGE_KEY);
    } catch {
      // Ignorer les restrictions éventuelles de sessionStorage
    }
  }
}
