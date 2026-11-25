import {
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  PLATFORM_ID,
  ViewChild,
  signal,
  computed,
  inject,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProblemDetail } from '@shared/models/api-response.model';
import { AuthService } from '../../../core/services/auth.service';
import { GoogleSsoService } from '../../../core/services/google-sso.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly googleSsoService = inject(GoogleSsoService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly platformId = inject(PLATFORM_ID);

  @ViewChild('googleBtnContainer')
  private googleBtnContainer?: ElementRef<HTMLDivElement>;

  readonly isDemoMode = !environment.production;

  // État du formulaire en Signal Forms purs
  readonly telephoneOuEmail = signal<string>('');
  readonly codeOtp = signal<string>('');
  readonly etape = signal<'IDENTIFIANT' | 'OTP' | 'LIER_TELEPHONE'>('IDENTIFIANT');
  readonly messageErreur = signal<string>('');
  readonly messageInfo = signal<string>('');
  readonly chargement = signal<boolean>(false);

  // Authentification SSO Google OIDC
  readonly isSocialLoading = signal<boolean>(false);
  readonly googleOidcPret = signal<boolean>(false);
  readonly googleDevFallbackActif = signal<boolean>(false);
  readonly telephoneLiaison = signal<string>('');
  readonly codeOtpLiaison = signal<string>('');
  readonly otpLiaisonEnvoye = signal<boolean>(false);

  // Validation réactive avec computed()
  readonly isIdentifiantValide = computed(() => {
    const val = this.telephoneOuEmail().trim();
    return (val.startsWith('+') && val.length >= 9) || val.length === 9 || val.includes('@');
  });

  readonly isOtpValide = computed(() => {
    return this.codeOtp().trim().length === 6;
  });

  readonly isTelephoneLiaisonValide = computed(() => {
    const val = this.telephoneLiaison().trim();
    return (val.startsWith('+') && val.length >= 9) || val.length === 9;
  });

  readonly isOtpLiaisonValide = computed(() => {
    return this.codeOtpLiaison().trim().length === 6;
  });

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.googleSsoService.credential$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((idToken) => this.finaliserConnexionGoogleOidc(idToken));

    this.authService
      .chargerConfigSso()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((config) => {
        this.googleDevFallbackActif.set(config.devFallbackEnabled);
        if (config.googleEnabled && config.googleClientId) {
          this.googleSsoService
            .initialize(config.googleClientId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((ready) => {
              this.googleOidcPret.set(ready);
              if (ready && this.googleBtnContainer?.nativeElement) {
                this.googleSsoService.renderSignInButton(this.googleBtnContainer.nativeElement);
              }
            });
        }
      });
  }

  onIdentifiantInput(val: string): void {
    this.telephoneOuEmail.set(val);
    this.messageErreur.set('');
  }

  onOtpInput(val: string): void {
    this.codeOtp.set(val);
    this.messageErreur.set('');
  }

  onTelephoneLiaisonInput(val: string): void {
    this.telephoneLiaison.set(val);
    this.messageErreur.set('');
    this.messageInfo.set('');
  }

  onOtpLiaisonInput(val: string): void {
    this.codeOtpLiaison.set(val);
    this.messageErreur.set('');
  }

  remplirNumeroDemo(): void {
    if (this.isDemoMode) {
      this.telephoneOuEmail.set('+237699123456');
      this.messageErreur.set('');
    }
  }

  accederDemo(): void {
    if (this.isDemoMode) {
      this.authService.connexionDemo();
    }
  }

  demanderOtp(): void {
    if (!this.isIdentifiantValide()) {
      this.messageErreur.set(
        'Veuillez saisir un numéro de téléphone international (+237..., +33...) ou un email valide.'
      );
      return;
    }

    this.messageErreur.set('');
    this.chargement.set(true);

    this.authService
      .demanderOtp(this.telephoneOuEmail().trim())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.chargement.set(false);
          this.codeOtp.set(this.isDemoMode ? '123456' : '');
          this.etape.set('OTP');
        },
        error: (err: unknown) => {
          this.chargement.set(false);
          this.messageErreur.set(
            this.extraireMessageErreur(
              err,
              "Impossible d'envoyer le code OTP. Vérifiez la connexion à la passerelle API."
            )
          );
        },
      });
  }

  validerOtp(): void {
    if (!this.isOtpValide()) {
      this.messageErreur.set('Le code OTP doit comporter 6 chiffres.');
      return;
    }

    this.messageErreur.set('');
    this.chargement.set(true);

    this.authService
      .verifierOtp(this.telephoneOuEmail().trim(), this.codeOtp().trim())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (succes) => {
          this.chargement.set(false);
          if (succes) {
            this.router.navigate(['/dashboard']);
          } else {
            this.messageErreur.set('Code OTP incorrect ou expiré.');
          }
        },
        error: (err: unknown) => {
          this.chargement.set(false);
          this.messageErreur.set(
            this.extraireMessageErreur(err, 'Code OTP incorrect ou expiré. Veuillez réessayer.')
          );
        },
      });
  }

  connexionGoogleFallbackOuPrompt(): void {
    this.messageErreur.set('');

    if (this.googleOidcPret()) {
      this.googleSsoService.promptSignIn();
      return;
    }

    if (!this.googleDevFallbackActif()) {
      this.messageErreur.set(
        'Google Sign-In nécessite la variable GOOGLE_CLIENT_ID côté serveur (voir SSO_GOOGLE_GUIDE.md).'
      );
      return;
    }

    this.isSocialLoading.set(true);
    this.authService
      .connexionSociale('GOOGLE')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          this.isSocialLoading.set(false);
          if (!resultat.succes) {
            this.messageErreur.set('Échec de la connexion avec Google.');
            return;
          }
          if (resultat.telephoneRequis) {
            this.etape.set('LIER_TELEPHONE');
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err: unknown) => {
          this.isSocialLoading.set(false);
          this.messageErreur.set(
            this.extraireMessageErreur(err, 'Impossible de se connecter via Google.')
          );
        },
      });
  }

  private finaliserConnexionGoogleOidc(idToken: string): void {
    this.messageErreur.set('');
    this.isSocialLoading.set(true);

    this.authService
      .connexionGoogleAvecToken(idToken)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          this.isSocialLoading.set(false);
          if (!resultat.succes) {
            this.messageErreur.set('Vérification Google OIDC refusée par le serveur.');
            return;
          }
          if (resultat.telephoneRequis) {
            this.etape.set('LIER_TELEPHONE');
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err: unknown) => {
          this.isSocialLoading.set(false);
          this.messageErreur.set(
            this.extraireMessageErreur(
              err,
              'Impossible de valider votre compte Google auprès du serveur Njangi.'
            )
          );
        },
      });
  }

  demanderOtpLiaisonTelephone(): void {
    if (!this.isTelephoneLiaisonValide()) {
      this.messageErreur.set(
        'Veuillez renseigner un numéro de téléphone valide (ex: +237699123456 ou +33612345678).'
      );
      return;
    }

    this.messageErreur.set('');
    this.messageInfo.set('');
    this.chargement.set(true);

    this.authService
      .demanderOtp(this.telephoneLiaison().trim())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.chargement.set(false);
          this.otpLiaisonEnvoye.set(true);
          this.codeOtpLiaison.set(this.isDemoMode ? '123456' : '');
          this.messageInfo.set(
            `Code OTP envoyé au ${this.telephoneLiaison().trim()}. Saisissez-le pour confirmer la liaison.`
          );
        },
        error: (err: unknown) => {
          this.chargement.set(false);
          this.messageErreur.set(
            this.extraireMessageErreur(err, "Impossible d'envoyer le code OTP de confirmation.")
          );
        },
      });
  }

  confirmerLiaisonTelephone(): void {
    if (!this.isTelephoneLiaisonValide()) {
      this.messageErreur.set(
        'Veuillez renseigner un numéro de téléphone valide (ex: +237699123456 ou +33612345678).'
      );
      return;
    }

    if (!this.otpLiaisonEnvoye()) {
      this.demanderOtpLiaisonTelephone();
      return;
    }

    if (!this.isOtpLiaisonValide()) {
      this.messageErreur.set('Veuillez saisir le code OTP à 6 chiffres reçu par SMS.');
      return;
    }

    this.messageErreur.set('');
    this.chargement.set(true);

    this.authService
      .lierTelephone(this.telephoneLiaison().trim(), this.codeOtpLiaison().trim())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (succes) => {
          this.chargement.set(false);
          if (succes) {
            this.router.navigate(['/dashboard']);
          } else {
            this.messageErreur.set('Impossible de lier ce numéro de téléphone.');
          }
        },
        error: (err: unknown) => {
          this.chargement.set(false);
          this.messageErreur.set(
            this.extraireMessageErreur(err, 'Erreur lors de la liaison du numéro de téléphone.')
          );
        },
      });
  }

  ignorerLiaisonTelephone(): void {
    this.router.navigate(['/dashboard']);
  }

  retourIdentifiant(): void {
    this.etape.set('IDENTIFIANT');
    this.messageErreur.set('');
    this.messageInfo.set('');
  }

  private extraireMessageErreur(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as (Partial<ProblemDetail> & { message?: string }) | string | null;
      if (typeof body === 'string' && body.trim().length > 0) {
        return body;
      }
      if (body && typeof body === 'object') {
        if (body.detail) {
          return body.detail;
        }
        if (body.message) {
          return body.message;
        }
        if (body.title) {
          return body.title;
        }
      }
    }
    if (error instanceof Error && error.message) {
      return error.message;
    }
    return fallback;
  }
}
