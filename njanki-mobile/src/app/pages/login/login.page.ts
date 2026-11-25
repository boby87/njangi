import {
  Component,
  ChangeDetectionStrategy,
  ElementRef,
  OnInit,
  ViewChild,
  inject,
  signal,
  computed,
  DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IonContent } from '@ionic/angular';
import { MobileAuthService } from '../../core/services/auth.service';
import { GoogleSsoMobileService } from '../../core/services/google-sso-mobile.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    IonContent,
  ],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage implements OnInit {
  readonly authService = inject(MobileAuthService);
  private readonly googleSso = inject(GoogleSsoMobileService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild('mobileGoogleBtnContainer')
  private mobileGoogleBtnContainer?: ElementRef<HTMLDivElement>;

  // État Signal Forms pur (aucun FormGroup, aucun ngModel)
  readonly loginMode = signal<'phone' | 'email'>('phone');
  readonly etape = signal<'FORM' | 'OTP' | 'LIER_TELEPHONE'>('FORM');
  readonly telephone = signal<string>('699 123 456');
  readonly email = signal<string>('');
  readonly codeSecret = signal<string>('');
  readonly showPassword = signal<boolean>(false);
  readonly otpCode = signal<string>('');
  readonly errorMessage = signal<string>('');
  readonly successMessage = signal<string>('');
  readonly isSubmitting = signal<boolean>(false);

  // État Google SSO OIDC & liaison téléphone
  readonly googleClientId = signal<string>('');
  readonly googleOidcPret = signal<boolean>(false);
  readonly googleDevFallbackActif = signal<boolean>(false);
  readonly linkPhoneOtpSent = signal<boolean>(false);
  readonly linkPhoneOtpCode = signal<string>('');

  // Validation réactive via computed()
  readonly cleanPhone = computed(() => {
    return this.telephone().replace(/\D/g, '');
  });

  readonly isPhoneValid = computed(() => {
    const raw = this.cleanPhone();
    return raw.length === 9 && /^[623][0-9]{8}$/.test(raw);
  });

  readonly isEmailValid = computed(() => {
    const e = this.email().trim();
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e);
  });

  readonly isCodeSecretValid = computed(() => {
    const c = this.codeSecret().trim();
    return c.length >= 4 && c.length <= 6 && /^\d+$/.test(c);
  });

  readonly isFormValid = computed(() => {
    if (this.loginMode() === 'phone') {
      return this.isPhoneValid() && this.isCodeSecretValid();
    }
    return this.isEmailValid() && this.isCodeSecretValid();
  });

  readonly isOtpValid = computed(() => {
    return this.otpCode().trim().length === 6;
  });

  readonly isLinkOtpValid = computed(() => {
    return this.linkPhoneOtpCode().trim().length === 6;
  });

  ngOnInit(): void {
    this.googleSso.credential$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((idToken) => this.finaliserGoogleIdToken(idToken));

    this.authService
      .chargerConfigSso()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((config) => {
        this.googleClientId.set(config.googleClientId);
        this.googleDevFallbackActif.set(config.devFallbackEnabled);
        if (config.googleEnabled && config.googleClientId) {
          this.googleSso
            .initialize(config.googleClientId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((ready) => {
              this.googleOidcPret.set(ready);
              if (ready && !this.googleSso.isNative && this.mobileGoogleBtnContainer?.nativeElement) {
                this.googleSso.renderWebButton(this.mobileGoogleBtnContainer.nativeElement);
              }
            });
        }
      });
  }

  switchLoginMode(mode: 'phone' | 'email'): void {
    this.loginMode.set(mode);
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  onPhoneInput(raw: string): void {
    let digits = raw.replace(/\D/g, '');
    if (digits.length > 9) {
      digits = digits.substring(0, 9);
    }

    let formatted = '';
    if (digits.length > 0) {
      formatted = digits.substring(0, 3);
      if (digits.length > 3) {
        formatted += ' ' + digits.substring(3, 6);
      }
      if (digits.length > 6) {
        formatted += ' ' + digits.substring(6, 9);
      }
    }

    this.telephone.set(formatted);
    this.errorMessage.set('');
  }

  onEmailInput(raw: string): void {
    this.email.set(raw.trim());
    this.errorMessage.set('');
  }

  onCodeSecretInput(raw: string): void {
    let digits = raw.replace(/\D/g, '');
    if (digits.length > 6) {
      digits = digits.substring(0, 6);
    }
    this.codeSecret.set(digits);
    this.errorMessage.set('');
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  onOtpInput(raw: string): void {
    let digits = raw.replace(/\D/g, '');
    if (digits.length > 6) {
      digits = digits.substring(0, 6);
    }
    this.otpCode.set(digits);
    this.errorMessage.set('');
  }

  onSubmitLogin(): void {
    if (!this.isFormValid()) {
      if (this.loginMode() === 'phone' && !this.isPhoneValid()) {
        this.errorMessage.set('Veuillez renseigner un numéro camerounais valide (9 chiffres commençant par 6, 2 ou 3).');
        return;
      }
      if (this.loginMode() === 'email' && !this.isEmailValid()) {
        this.errorMessage.set('Veuillez renseigner une adresse email valide.');
        return;
      }
      if (!this.isCodeSecretValid()) {
        this.errorMessage.set('Le code secret doit comporter entre 4 et 6 chiffres.');
        return;
      }
    }

    const identifiant =
      this.loginMode() === 'phone' ? '+237' + this.cleanPhone() : this.email().trim();

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .loginWithPassword(identifiant, this.codeSecret())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (success) => {
          this.isSubmitting.set(false);
          if (success) {
            this.router.navigate(['/tabs/dashboard']);
          } else {
            this.errorMessage.set(
              this.authService.authError() ||
                'Identifiants incorrects. Veuillez vérifier vos informations.'
            );
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Passerelle API indisponible. Veuillez réessayer.');
        },
      });
  }

  onTriggerOtpLogin(): void {
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Veuillez d\'abord saisir un numéro de téléphone valide pour recevoir le code SMS.');
      return;
    }

    const fullPhone = '+237' + this.cleanPhone();
    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .requestOtp(fullPhone)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (sent) => {
          this.isSubmitting.set(false);
          if (sent) {
            this.otpCode.set(this.authService.simulatedOtp() || '');
            this.etape.set('OTP');
          } else {
            this.errorMessage.set(
              this.authService.authError() || 'Impossible d\'envoyer le code OTP par SMS.'
            );
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Impossible d\'envoyer le code OTP par SMS.');
        },
      });
  }

  onVerifyOtp(): void {
    if (!this.isOtpValid()) {
      this.errorMessage.set('Le code OTP doit comporter 6 chiffres.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .verifyOtp(this.otpCode())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (ok) => {
          this.isSubmitting.set(false);
          if (ok) {
            this.router.navigate(['/tabs/dashboard']);
          } else {
            this.errorMessage.set(
              this.authService.authError() || 'Code OTP invalide ou expiré. Veuillez réessayer.'
            );
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Erreur lors de la vérification du code OTP.');
        },
      });
  }

  onBiometricLogin(): void {
    this.errorMessage.set('');
    const storedPin = this.authService.codePin();
    if (!storedPin || !this.isPhoneValid()) {
      this.errorMessage.set('Veuillez vous connecter une première fois avec votre code secret pour activer la biométrie.');
      return;
    }

    const identifiant = '+237' + this.cleanPhone();
    this.isSubmitting.set(true);

    this.authService
      .loginWithPassword(identifiant, storedPin)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (success) => {
          this.isSubmitting.set(false);
          if (success) {
            this.router.navigate(['/tabs/dashboard']);
          } else {
            this.errorMessage.set(
              this.authService.authError() || 'Authentification biométrique refusée.'
            );
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Erreur lors de l\'authentification biométrique.');
        },
      });
  }

  onLinkOtpInput(raw: string): void {
    let digits = raw.replace(/\D/g, '');
    if (digits.length > 6) {
      digits = digits.substring(0, 6);
    }
    this.linkPhoneOtpCode.set(digits);
    this.errorMessage.set('');
  }

  onSsoLogin(provider: 'GOOGLE'): void {
    if (provider !== 'GOOGLE') {
      return;
    }

    this.errorMessage.set('');
    const clientId = this.googleClientId();

    if (this.googleSso.isNative && clientId) {
      this.isSubmitting.set(true);
      this.googleSso
        .loginNativeGoogle(clientId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (idToken) => this.finaliserGoogleIdToken(idToken),
          error: (err: unknown) => {
            this.isSubmitting.set(false);
            this.errorMessage.set(
              err instanceof Error ? err.message : 'Connexion Google Android annulée ou indisponible.'
            );
          },
        });
      return;
    }

    if (this.googleOidcPret()) {
      this.googleSso.promptWebSignIn();
      return;
    }

    if (!this.googleDevFallbackActif()) {
      this.errorMessage.set(
        'Google Sign-In nécessite la configuration de GOOGLE_CLIENT_ID sur le serveur.'
      );
      return;
    }

    this.isSubmitting.set(true);
    this.authService
      .connexionSociale('GOOGLE')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.isSubmitting.set(false);
          if (res.succes) {
            if (res.telephoneRequis) {
              this.etape.set('LIER_TELEPHONE');
            } else {
              this.router.navigate(['/tabs/dashboard']);
            }
          } else {
            this.errorMessage.set(
              this.authService.authError() || 'Échec de la connexion Google.'
            );
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Erreur réseau lors de la connexion Google.');
        },
      });
  }

  private finaliserGoogleIdToken(idToken: string): void {
    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .connexionGoogleAvecToken(idToken)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.isSubmitting.set(false);
          if (!res.succes) {
            this.errorMessage.set(
              this.authService.authError() || 'Échec de la vérification Google OIDC.'
            );
            return;
          }
          if (res.telephoneRequis) {
            this.etape.set('LIER_TELEPHONE');
          } else {
            this.router.navigate(['/tabs/dashboard']);
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Erreur réseau lors de la validation Google.');
        },
      });
  }

  onRequestLinkPhoneOtp(): void {
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Veuillez saisir un numéro camerounais valide (9 chiffres).');
      return;
    }

    const fullPhone = '+237' + this.cleanPhone();
    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .requestOtp(fullPhone)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (sent) => {
          this.isSubmitting.set(false);
          if (sent) {
            this.linkPhoneOtpSent.set(true);
            this.successMessage.set(`Code OTP envoyé au ${fullPhone}.`);
          } else {
            this.errorMessage.set(this.authService.authError() || "Échec de l'envoi du code OTP.");
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set("Impossible d'envoyer le code OTP.");
        },
      });
  }

  onConfirmLinkPhone(): void {
    if (!this.linkPhoneOtpSent()) {
      this.onRequestLinkPhoneOtp();
      return;
    }
    if (!this.isLinkOtpValid()) {
      this.errorMessage.set('Le code OTP doit comporter 6 chiffres.');
      return;
    }

    const fullPhone = '+237' + this.cleanPhone();
    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .lierTelephone(fullPhone, this.linkPhoneOtpCode())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (ok) => {
          this.isSubmitting.set(false);
          if (ok) {
            this.router.navigate(['/tabs/dashboard']);
          } else {
            this.errorMessage.set(
              this.authService.authError() || 'Impossible de lier ce numéro de téléphone.'
            );
          }
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Erreur réseau lors de la liaison du téléphone.');
        },
      });
  }

  onSkipLinkPhone(): void {
    this.router.navigate(['/tabs/dashboard']);
  }

  onForgotPassword(): void {
    this.errorMessage.set('Un lien de réinitialisation sécurisé sera envoyé à votre contact de confiance ou par SMS.');
  }

  onRetourForm(): void {
    this.authService.isAwaitingOtp.set(false);
    this.etape.set('FORM');
    this.errorMessage.set('');
  }

  onAllerInscription(): void {
    this.router.navigate(['/register']);
  }

  onRetour(): void {
    this.router.navigate(['/tabs/dashboard']);
  }
}
