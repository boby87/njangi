import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Injectable, NgZone, PLATFORM_ID, inject, signal } from '@angular/core';
import { Observable, Subject, of } from 'rxjs';

interface GoogleCredentialResponse {
  credential?: string;
  select_by?: string;
}

interface GoogleIdConfiguration {
  client_id: string;
  callback: (response: GoogleCredentialResponse) => void;
  auto_select?: boolean;
  cancel_on_tap_outside?: boolean;
  context?: 'signin' | 'signup' | 'use';
}

interface GoogleButtonConfiguration {
  type?: 'standard' | 'icon';
  theme?: 'outline' | 'filled_blue' | 'filled_black';
  size?: 'large' | 'medium' | 'small';
  text?: 'signin_with' | 'signup_with' | 'continue_with' | 'signin';
  shape?: 'rectangular' | 'pill' | 'circle' | 'square';
  logo_alignment?: 'left' | 'center';
  width?: number;
  locale?: string;
}

interface GoogleAccountsIdApi {
  initialize: (config: GoogleIdConfiguration) => void;
  renderButton: (parent: HTMLElement, options: GoogleButtonConfiguration) => void;
  prompt: () => void;
  disableAutoSelect: () => void;
}

interface GoogleGlobalNamespace {
  accounts?: {
    id?: GoogleAccountsIdApi;
  };
}

interface WindowWithGoogle extends Window {
  google?: GoogleGlobalNamespace;
}

const GIS_SCRIPT_URL = 'https://accounts.google.com/gsi/client';
const GIS_SCRIPT_ID = 'google-identity-services-sdk';

/**
 * Service d'intégration Google Identity Services (OIDC) pour Angular 22.
 * 100% RxJS & Signals (zéro Promise / async / await, SSR-safe).
 */
@Injectable({
  providedIn: 'root',
})
export class GoogleSsoService {
  private readonly document = inject(DOCUMENT);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly ngZone = inject(NgZone);

  private readonly credentialSubject = new Subject<string>();
  private initializedClientId = '';

  readonly sdkLoaded = signal<boolean>(false);
  readonly sdkError = signal<string>('');
  readonly credential$: Observable<string> = this.credentialSubject.asObservable();

  initialize(clientId: string): Observable<boolean> {
    if (!isPlatformBrowser(this.platformId)) {
      return of(false);
    }

    const trimmedClientId = clientId.trim();
    if (!trimmedClientId) {
      return of(false);
    }

    return new Observable<boolean>((subscriber) => {
      const initGis = (): void => {
        const gisApi = this.getGisApi();
        if (!gisApi) {
          this.sdkError.set('Google Identity Services indisponible dans ce navigateur.');
          subscriber.next(false);
          subscriber.complete();
          return;
        }

        if (this.initializedClientId !== trimmedClientId) {
          gisApi.initialize({
            client_id: trimmedClientId,
            callback: (response: GoogleCredentialResponse) => {
              if (response.credential) {
                this.ngZone.run(() => {
                  this.credentialSubject.next(response.credential as string);
                });
              }
            },
            auto_select: false,
            cancel_on_tap_outside: true,
            context: 'signin',
          });
          this.initializedClientId = trimmedClientId;
        }

        this.sdkLoaded.set(true);
        this.sdkError.set('');
        subscriber.next(true);
        subscriber.complete();
      };

      if (this.getGisApi()) {
        initGis();
        return;
      }

      const existingScript = this.document.getElementById(GIS_SCRIPT_ID) as HTMLScriptElement | null;
      if (existingScript) {
        const onLoad = (): void => {
          existingScript.removeEventListener('load', onLoad);
          existingScript.removeEventListener('error', onError);
          initGis();
        };
        const onError = (): void => {
          existingScript.removeEventListener('load', onLoad);
          existingScript.removeEventListener('error', onError);
          this.sdkError.set('Impossible de charger le SDK Google Identity Services.');
          subscriber.next(false);
          subscriber.complete();
        };
        existingScript.addEventListener('load', onLoad);
        existingScript.addEventListener('error', onError);
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
        initGis();
      };

      const onError = (): void => {
        script.removeEventListener('load', onLoad);
        script.removeEventListener('error', onError);
        this.sdkError.set('Impossible de charger le SDK Google (vérifiez la connexion réseau).');
        subscriber.next(false);
        subscriber.complete();
      };

      script.addEventListener('load', onLoad);
      script.addEventListener('error', onError);
      this.document.head.appendChild(script);
    });
  }

  renderSignInButton(container: HTMLElement): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return false;
    }

    const gisApi = this.getGisApi();
    if (!gisApi) {
      return false;
    }

    container.innerHTML = '';
    const width = Math.max(260, Math.min(380, container.clientWidth || 360));
    gisApi.renderButton(container, {
      type: 'standard',
      theme: 'filled_black',
      size: 'large',
      text: 'continue_with',
      shape: 'pill',
      logo_alignment: 'left',
      width,
      locale: 'fr',
    });
    return true;
  }

  promptSignIn(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    this.getGisApi()?.prompt();
  }

  disableAutoSelect(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    this.getGisApi()?.disableAutoSelect();
  }

  private getGisApi(): GoogleAccountsIdApi | undefined {
    const win = this.document.defaultView as WindowWithGoogle | null;
    return win?.google?.accounts?.id;
  }
}
