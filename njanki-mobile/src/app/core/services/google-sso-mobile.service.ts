import { DOCUMENT } from '@angular/common';
import { Injectable, NgZone, inject, signal } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { Observable, Subject, from, of, throwError } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

interface GoogleCredentialResponse {
  credential?: string;
}

interface GoogleIdConfiguration {
  client_id: string;
  callback: (response: GoogleCredentialResponse) => void;
  auto_select?: boolean;
  cancel_on_tap_outside?: boolean;
}

interface GoogleButtonConfiguration {
  type?: 'standard' | 'icon';
  theme?: 'outline' | 'filled_blue' | 'filled_black';
  size?: 'large' | 'medium' | 'small';
  text?: 'signin_with' | 'signup_with' | 'continue_with' | 'signin';
  shape?: 'rectangular' | 'pill';
  width?: number;
  locale?: string;
}

interface GoogleAccountsIdApi {
  initialize: (config: GoogleIdConfiguration) => void;
  renderButton: (parent: HTMLElement, options: GoogleButtonConfiguration) => void;
  prompt: () => void;
}

interface NativeSocialLoginResult {
  result?: {
    idToken?: string;
    accessToken?: { token?: string };
  };
}

interface NativeSocialLoginPlugin {
  initialize: (options: { google: { webClientId: string } }) => Promise<void>;
  login: (options: {
    provider: 'google';
    options: { scopes: string[] };
  }) => Promise<NativeSocialLoginResult>;
}

interface WindowWithSso extends Window {
  google?: {
    accounts?: {
      id?: GoogleAccountsIdApi;
    };
  };
  Capacitor?: {
    Plugins?: {
      SocialLogin?: NativeSocialLoginPlugin;
    };
  };
}

const GIS_SCRIPT_URL = 'https://accounts.google.com/gsi/client';
const GIS_SCRIPT_ID = 'njangi-mobile-gis-sdk';

/**
 * Service Google SSO hybride (Capacitor Android natif + Web/PWA GIS).
 * Strictement basé sur RxJS (Observable / from()) et Signals — zéro Promise / async / await.
 */
@Injectable({
  providedIn: 'root',
})
export class GoogleSsoMobileService {
  private readonly document = inject(DOCUMENT);
  private readonly ngZone = inject(NgZone);

  private readonly credentialSubject = new Subject<string>();
  private initializedClientId = '';

  readonly isNative = Capacitor.isNativePlatform();
  readonly sdkReady = signal<boolean>(false);
  readonly credential$: Observable<string> = this.credentialSubject.asObservable();

  initialize(webClientId: string): Observable<boolean> {
    const trimmed = webClientId.trim();
    if (!trimmed) {
      return of(false);
    }

    if (this.isNative) {
      const nativePlugin = this.getNativePlugin();
      if (!nativePlugin) {
        return of(false);
      }
      return from(nativePlugin.initialize({ google: { webClientId: trimmed } })).pipe(
        map(() => {
          this.initializedClientId = trimmed;
          this.sdkReady.set(true);
          return true;
        })
      );
    }

    return this.initializeWebGis(trimmed);
  }

  loginNativeGoogle(webClientId: string): Observable<string> {
    const nativePlugin = this.getNativePlugin();
    if (!nativePlugin) {
      return throwError(
        () =>
          new Error(
            'Plugin natif @capgo/capacitor-social-login non installé dans le build Android (voir SSO_GOOGLE_GUIDE.md).'
          )
      );
    }

    const ensureInit$ =
      this.initializedClientId === webClientId.trim()
        ? of(true)
        : this.initialize(webClientId);

    return ensureInit$.pipe(
      switchMap(() =>
        from(
          nativePlugin.login({
            provider: 'google',
            options: { scopes: ['email', 'profile'] },
          })
        )
      ),
      map((res) => {
        const idToken = res?.result?.idToken;
        if (!idToken) {
          throw new Error('Google n’a pas retourné de jeton ID Token valide.');
        }
        return idToken;
      })
    );
  }

  renderWebButton(container: HTMLElement): boolean {
    if (this.isNative) {
      return false;
    }
    const gisApi = this.getGisApi();
    if (!gisApi) {
      return false;
    }

    container.innerHTML = '';
    const width = Math.max(260, Math.min(380, container.clientWidth || 340));
    gisApi.renderButton(container, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      shape: 'rectangular',
      width,
      locale: 'fr',
    });
    return true;
  }

  promptWebSignIn(): void {
    if (!this.isNative) {
      this.getGisApi()?.prompt();
    }
  }

  private initializeWebGis(clientId: string): Observable<boolean> {
    return new Observable<boolean>((subscriber) => {
      const init = (): void => {
        const gisApi = this.getGisApi();
        if (!gisApi) {
          subscriber.next(false);
          subscriber.complete();
          return;
        }

        if (this.initializedClientId !== clientId) {
          gisApi.initialize({
            client_id: clientId,
            callback: (response: GoogleCredentialResponse) => {
              if (response.credential) {
                this.ngZone.run(() => {
                  this.credentialSubject.next(response.credential as string);
                });
              }
            },
            auto_select: false,
            cancel_on_tap_outside: true,
          });
          this.initializedClientId = clientId;
        }

        this.sdkReady.set(true);
        subscriber.next(true);
        subscriber.complete();
      };

      if (this.getGisApi()) {
        init();
        return;
      }

      const existing = this.document.getElementById(GIS_SCRIPT_ID) as HTMLScriptElement | null;
      if (existing) {
        const onLoad = (): void => {
          existing.removeEventListener('load', onLoad);
          existing.removeEventListener('error', onError);
          init();
        };
        const onError = (): void => {
          existing.removeEventListener('load', onLoad);
          existing.removeEventListener('error', onError);
          subscriber.next(false);
          subscriber.complete();
        };
        existing.addEventListener('load', onLoad);
        existing.addEventListener('error', onError);
        return;
      }

      const script = this.document.createElement('script');
      script.id = GIS_SCRIPT_ID;
      script.src = GIS_SCRIPT_URL;
      script.async = true;
      script.defer = true;

      const onLoad = (): void => {
        script.removeEventListener('load', onLoad);
        script.removeEventListener('error', onError);
        init();
      };
      const onError = (): void => {
        script.removeEventListener('load', onLoad);
        script.removeEventListener('error', onError);
        subscriber.next(false);
        subscriber.complete();
      };

      script.addEventListener('load', onLoad);
      script.addEventListener('error', onError);
      this.document.head.appendChild(script);
    });
  }

  private getGisApi(): GoogleAccountsIdApi | undefined {
    const win = this.document.defaultView as WindowWithSso | null;
    return win?.google?.accounts?.id;
  }

  private getNativePlugin(): NativeSocialLoginPlugin | undefined {
    const win = this.document.defaultView as WindowWithSso | null;
    return win?.Capacitor?.Plugins?.SocialLogin;
  }
}
