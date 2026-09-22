import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { IonContent } from '@ionic/angular';
import { MobileAuthService } from '../../core/services/auth.service';

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
export class LoginPage {
  readonly authService = inject(MobileAuthService);
  private readonly router = inject(Router);

  // État Signal Forms pur (aucun FormGroup, aucun ngModel)
  readonly loginMode = signal<'phone' | 'email'>('phone');
  readonly etape = signal<'FORM' | 'OTP'>('FORM');
  readonly telephone = signal<string>('699 123 456');
  readonly email = signal<string>('');
  readonly codeSecret = signal<string>('1234');
  readonly showPassword = signal<boolean>(false);
  readonly otpCode = signal<string>('');
  readonly errorMessage = signal<string>('');
  readonly successMessage = signal<string>('');
  readonly isSubmitting = signal<boolean>(false);

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

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    // Connexion directe avec le code secret
    const success = this.authService.verifyOtp('123456');
    if (success) {
      this.router.navigate(['/tabs/dashboard']);
    } else {
      this.errorMessage.set('Identifiants incorrects. Veuillez vérifier vos informations.');
    }
    this.isSubmitting.set(false);
  }

  onTriggerOtpLogin(): void {
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Veuillez d\'abord saisir un numéro de téléphone valide pour recevoir le code SMS.');
      return;
    }

    const fullPhone = '+237' + this.cleanPhone();
    this.authService.requestOtp(fullPhone);
    this.otpCode.set(this.authService.simulatedOtp() || '884210');
    this.etape.set('OTP');
    this.errorMessage.set('');
  }

  onVerifyOtp(): void {
    if (!this.isOtpValid()) {
      this.errorMessage.set('Le code OTP doit comporter 6 chiffres.');
      return;
    }

    const ok = this.authService.verifyOtp(this.otpCode());
    if (ok) {
      this.router.navigate(['/tabs/dashboard']);
    } else {
      this.errorMessage.set('Code OTP invalide ou expiré. Veuillez réessayer.');
    }
  }

  onBiometricLogin(): void {
    this.errorMessage.set('');
    // Simulation du capteur biométrique (WebAuthn / Biométrie native Capacitor)
    const success = this.authService.verifyOtp('123456');
    if (success) {
      this.router.navigate(['/tabs/dashboard']);
    }
  }

  onSsoLogin(provider: 'GOOGLE' | 'APPLE'): void {
    this.errorMessage.set('');
    this.authService.connexionSociale(provider === 'GOOGLE' ? 'GOOGLE' : 'FACEBOOK');
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
