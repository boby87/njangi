import { Injectable, signal, computed, inject, DestroyRef } from '@angular/core';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Preferences } from '@capacitor/preferences';
import { Observable, forkJoin, from, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Membre, RoleMembre, StatutMembre } from '@shared/models/membre.model';
import { environment } from '../../../environments/environment';
import { MobileApiService } from './api.service';

export interface MobileSsoPublicConfig {
  googleEnabled: boolean;
  googleClientId: string;
  devFallbackEnabled: boolean;
}

export const TOKEN_KEY = 'njangi_auth_token';
export const USER_KEY = 'njangi_current_user';
export const PIN_KEY = 'njangi_user_pin';

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

export interface SocialAuthPayload {
  providerId: string;
  email: string;
  nom: string;
  prenom: string;
  token?: string;
  photoUrl?: string;
}

interface GatewayUtilisateurDto {
  id: string;
  telephone?: string | null;
  email?: string | null;
  nom: string;
  prenom: string;
  photoUrl?: string | null;
  ville?: string | null;
  pays?: string | null;
  statut?: string | null;
  creeLe?: string | null;
}

interface GatewayLoginResponse {
  accessToken: string;
  tokenType?: string;
  expiresIn?: number;
  utilisateur: GatewayUtilisateurDto;
}

interface GatewaySocialLoginResponse extends GatewayLoginResponse {
  telephoneRequis: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class MobileAuthService {
  private readonly apiService = inject(MobileApiService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly currentUser = signal<Membre | null>(null);
  readonly activeRole = signal<RoleMembre>('MEMBRE');
  readonly token = signal<string | null>(null);
  readonly codePin = signal<string | null>(null);
  readonly sessionReady = signal<boolean>(false);
  readonly biometrieActivee = signal<boolean>(false);
  readonly simulatedOtp = signal<string | null>(null);
  readonly isAwaitingOtp = signal<boolean>(false);
  readonly pendingPhone = signal<string>('');
  readonly authError = signal<string | null>(null);

  readonly isAuthenticated = computed(() => !!this.token() && !!this.currentUser());

  constructor() {
    this.restoreSessionFromPreferences();
  }

  private restoreSessionFromPreferences(): void {
    forkJoin([
      from(Preferences.get({ key: TOKEN_KEY })).pipe(catchError(() => of({ value: null }))),
      from(Preferences.get({ key: USER_KEY })).pipe(catchError(() => of({ value: null }))),
      from(Preferences.get({ key: PIN_KEY })).pipe(catchError(() => of({ value: null }))),
    ])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ([tokenRes, userRes, pinRes]) => {
          if (tokenRes.value) {
            this.token.set(tokenRes.value);
          }

          if (userRes.value) {
            try {
              const parsed = JSON.parse(userRes.value) as Membre;
              if (parsed && parsed.id) {
                this.currentUser.set(parsed);
              }
            } catch {
              this.currentUser.set(null);
            }
          }

          if (pinRes.value) {
            this.codePin.set(pinRes.value);
          }

          this.sessionReady.set(true);
        },
        error: () => {
          this.sessionReady.set(true);
        },
      });
  }

  requestOtp(telephone: string): Observable<boolean> {
    const cleanedPhone = telephone.trim();
    this.pendingPhone.set(cleanedPhone);
    this.authError.set(null);

    return this.apiService
      .post<null>('/api/v1/auth/otp/demander', { identifiant: cleanedPhone })
      .pipe(
        map((res) => {
          if (res.success) {
            this.isAwaitingOtp.set(true);
            return true;
          }
          this.authError.set(res.message || 'Impossible de demander le code OTP.');
          return false;
        }),
        catchError(() => {
          this.authError.set('Passerelle API inaccessible pour la demande OTP.');
          return of(false);
        })
      );
  }

  verifyOtp(code: string): Observable<boolean> {
    this.authError.set(null);

    return this.apiService
      .post<GatewayLoginResponse>('/api/v1/auth/otp/verifier', {
        identifiant: this.pendingPhone(),
        code: code.trim(),
      })
      .pipe(
        switchMap((res) => {
          if (!res.success || !res.data?.accessToken || !res.data?.utilisateur) {
            this.authError.set(res.message || 'Code OTP invalide ou expiré.');
            return of(false);
          }

          const user = this.mapUtilisateurToMembre(res.data.utilisateur, {
            telephone: this.pendingPhone(),
          });
          return this.persistSession(res.data.accessToken, user).pipe(map(() => true));
        }),
        catchError(() => {
          this.authError.set('Erreur lors de la vérification du code OTP.');
          return of(false);
        })
      );
  }

  loginWithPassword(identifiant: string, motDePasse: string): Observable<boolean> {
    this.authError.set(null);

    return this.apiService
      .post<GatewayLoginResponse>('/api/v1/auth/login', {
        identifiant: identifiant.trim(),
        motDePasse,
      })
      .pipe(
        switchMap((res) => {
          if (!res.success || !res.data?.accessToken || !res.data?.utilisateur) {
            this.authError.set(res.message || 'Identifiant ou mot de passe incorrect.');
            return of(false);
          }

          const user = this.mapUtilisateurToMembre(res.data.utilisateur);
          return this.persistSession(res.data.accessToken, user).pipe(map(() => true));
        }),
        catchError(() => {
          this.authError.set('Erreur réseau lors de la connexion.');
          return of(false);
        })
      );
  }

  inscrire(data: InscriptionMobileData): Observable<Membre | null> {
    this.authError.set(null);

    const requestBody = {
      telephone: data.telephone.trim(),
      email: data.email?.trim() || null,
      motDePasse: data.motDePasse,
      nom: data.nom.trim(),
      prenom: data.prenom.trim(),
      photoUrl: data.avatarUrl || null,
      ville: data.ville.trim(),
      pays: data.pays?.trim() || 'Cameroun',
    };

    return this.apiService.post<GatewayLoginResponse>('/api/v1/auth/inscrire', requestBody).pipe(
      switchMap((res) => {
        if (!res.success || !res.data?.accessToken || !res.data?.utilisateur) {
          this.authError.set(res.message || "Échec de l'inscription.");
          return of(null);
        }

        const nouveauMembre = this.mapUtilisateurToMembre(res.data.utilisateur, {
          telephone: data.telephone.trim(),
          email: data.email?.trim() || '',
          nom: data.nom.trim(),
          prenom: data.prenom.trim(),
          ville: data.ville.trim(),
          pays: data.pays?.trim() || 'Cameroun',
          estVerifieKyc: !!(data.kycDocumentType && data.kycDocumentNumero),
        });

        this.activeRole.set('MEMBRE');
        return this.persistSession(res.data.accessToken, nouveauMembre, data.codePin).pipe(
          map(() => nouveauMembre)
        );
      }),
      catchError(() => {
        this.authError.set("Erreur réseau lors de l'inscription.");
        return of(null);
      })
    );
  }

  chargerConfigSso(): Observable<MobileSsoPublicConfig> {
    return this.apiService.get<MobileSsoPublicConfig>('/api/v1/auth/social/config').pipe(
      map((res) => ({
        googleEnabled: Boolean(res.data?.googleEnabled || environment.googleWebClientId),
        googleClientId: environment.googleWebClientId || res.data?.googleClientId || '',
        devFallbackEnabled: Boolean(res.data?.devFallbackEnabled),
      })),
      catchError(() =>
        of({
          googleEnabled: Boolean(environment.googleWebClientId),
          googleClientId: environment.googleWebClientId || '',
          devFallbackEnabled: !environment.production,
        })
      )
    );
  }

  connexionGoogleAvecToken(
    idToken: string
  ): Observable<{ succes: boolean; telephoneRequis: boolean }> {
    this.authError.set(null);

    return this.apiService
      .post<GatewaySocialLoginResponse>('/api/v1/auth/social/login', {
        provider: 'GOOGLE',
        token: idToken,
      })
      .pipe(
        switchMap((res) => {
          if (!res.success || !res.data?.accessToken || !res.data?.utilisateur) {
            this.authError.set(res.message || 'Vérification Google OIDC refusée.');
            return of({ succes: false, telephoneRequis: false });
          }

          const user = this.mapUtilisateurToMembre(res.data.utilisateur);
          const telephoneRequis = !!res.data.telephoneRequis;
          return this.persistSession(res.data.accessToken, user).pipe(
            map(() => ({ succes: true, telephoneRequis }))
          );
        }),
        catchError(() => {
          this.authError.set('Impossible de valider la connexion Google auprès de la passerelle.');
          return of({ succes: false, telephoneRequis: false });
        })
      );
  }

  connexionSociale(
    provider: 'GOOGLE' | 'FACEBOOK',
    payload?: SocialAuthPayload
  ): Observable<{ succes: boolean; telephoneRequis: boolean }> {
    this.authError.set(null);

    const requestBody = payload?.token
      ? {
          provider,
          token: payload.token,
        }
      : {
          provider,
          token: '',
          providerId: payload?.providerId ?? `${provider.toLowerCase()}-${Date.now()}`,
          email: payload?.email ?? '',
          nom: payload?.nom ?? 'Njangi',
          prenom: payload?.prenom ?? 'Membre',
          photoUrl: payload?.photoUrl ?? null,
        };

    return this.apiService
      .post<GatewaySocialLoginResponse>('/api/v1/auth/social/login', requestBody)
      .pipe(
        switchMap((res) => {
          if (!res.success || !res.data?.accessToken || !res.data?.utilisateur) {
            this.authError.set(res.message || 'Échec de la connexion sociale.');
            return of({ succes: false, telephoneRequis: false });
          }

          const user = this.mapUtilisateurToMembre(res.data.utilisateur);
          const telephoneRequis = !!res.data.telephoneRequis;
          return this.persistSession(res.data.accessToken, user).pipe(
            map(() => ({ succes: true, telephoneRequis }))
          );
        }),
        catchError(() => {
          this.authError.set('Erreur réseau lors de la connexion sociale.');
          return of({ succes: false, telephoneRequis: false });
        })
      );
  }

  lierTelephone(telephone: string, codeOtp?: string): Observable<boolean> {
    this.authError.set(null);
    const cleanedPhone = telephone.trim();

    return this.apiService
      .post<GatewayLoginResponse>('/api/v1/auth/social/lier-telephone', {
        telephone: cleanedPhone,
        codeOtp: codeOtp ?? null,
      })
      .pipe(
        switchMap((res) => {
          if (!res.success) {
            this.authError.set(res.message || 'Impossible de lier ce numéro de téléphone.');
            return of(false);
          }

          const current = this.currentUser();
          const updatedToken = res.data?.accessToken || this.token();
          const updatedUser = res.data?.utilisateur
            ? this.mapUtilisateurToMembre(res.data.utilisateur, { telephone: cleanedPhone })
            : current
              ? { ...current, telephone: cleanedPhone }
              : null;

          if (!updatedUser) {
            return of(false);
          }

          if (updatedToken) {
            return this.persistSession(updatedToken, updatedUser).pipe(map(() => true));
          }

          this.currentUser.set(updatedUser);
          return from(Preferences.set({ key: USER_KEY, value: JSON.stringify(updatedUser) })).pipe(
            catchError(() => of(undefined)),
            map(() => true)
          );
        }),
        catchError(() => {
          this.authError.set('Erreur réseau lors de la liaison du téléphone.');
          return of(false);
        })
      );
  }

  setActiveRole(role: RoleMembre): void {
    this.activeRole.set(role);
  }

  logout(): void {
    this.currentUser.set(null);
    this.token.set(null);
    this.codePin.set(null);
    this.activeRole.set('MEMBRE');
    this.isAwaitingOtp.set(false);
    this.pendingPhone.set('');
    this.authError.set(null);

    forkJoin([
      from(Preferences.remove({ key: TOKEN_KEY })).pipe(catchError(() => of(undefined))),
      from(Preferences.remove({ key: USER_KEY })).pipe(catchError(() => of(undefined))),
      from(Preferences.remove({ key: PIN_KEY })).pipe(catchError(() => of(undefined))),
    ])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe();

    this.router.navigate(['/login']);
  }

  private persistSession(token: string, user: Membre, pin?: string | null): Observable<void> {
    this.token.set(token);
    this.currentUser.set(user);
    this.isAwaitingOtp.set(false);
    this.authError.set(null);

    const tasks: Array<Observable<void>> = [
      from(Preferences.set({ key: TOKEN_KEY, value: token })).pipe(catchError(() => of(undefined))),
      from(Preferences.set({ key: USER_KEY, value: JSON.stringify(user) })).pipe(
        catchError(() => of(undefined))
      ),
    ];

    if (pin !== undefined && pin !== null) {
      this.codePin.set(pin);
      tasks.push(
        from(Preferences.set({ key: PIN_KEY, value: pin })).pipe(catchError(() => of(undefined)))
      );
    }

    return forkJoin(tasks).pipe(map(() => undefined));
  }

  private mapUtilisateurToMembre(
    dto: GatewayUtilisateurDto,
    fallback?: Partial<Membre>
  ): Membre {
    const nowIso = new Date().toISOString();
    const statut = (dto.statut as StatutMembre | undefined) || fallback?.statut || 'ACTIF';
    const createdAt = dto.creeLe || fallback?.createdAt || nowIso;

    return {
      id: dto.id,
      authUtilisateurId: dto.id,
      nom: dto.nom || fallback?.nom || '',
      prenom: dto.prenom || fallback?.prenom || '',
      telephone: dto.telephone ?? fallback?.telephone ?? '',
      email: dto.email ?? fallback?.email ?? '',
      avatarUrl: dto.photoUrl ?? fallback?.avatarUrl ?? undefined,
      photoUrl: dto.photoUrl ?? fallback?.photoUrl ?? undefined,
      ville: dto.ville ?? fallback?.ville ?? 'Douala',
      pays: dto.pays ?? fallback?.pays ?? 'Cameroun',
      statut,
      dateInscription: createdAt.substring(0, 10),
      createdAt,
      estVerifieKyc: fallback?.estVerifieKyc ?? false,
    };
  }
}
