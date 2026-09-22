import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { IonContent } from '@ionic/angular';
import { MobileAuthService } from '../../core/services/auth.service';

export interface CountryOption {
  code: string;
  flag: string;
  name: string;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, IonContent],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  readonly authService = inject(MobileAuthService);
  private readonly router = inject(Router);

  // État Signal Forms pur
  readonly etape = signal<'FORM' | 'OTP'>('FORM');
  readonly fullName = signal<string>('');
  readonly countryCode = signal<string>('+237');
  readonly phoneNumber = signal<string>('');
  readonly cityResidence = signal<string>('Douala, Cameroun');
  readonly pinDigits = signal<string[]>(['', '', '', '']);
  readonly showPin = signal<boolean>(false);
  readonly cguConsent = signal<boolean>(true);
  readonly otpCode = signal<string>('');
  readonly errorMessage = signal<string>('');
  readonly successMessage = signal<string>('');
  readonly isSubmitting = signal<boolean>(false);

  // Liste des pays & indicatifs
  readonly countries: CountryOption[] = [
    { code: '+237', flag: '🇨🇲', name: '+237' },
    { code: '+33', flag: '🇫🇷', name: '+33' },
    { code: '+1', flag: '🇺🇸', name: '+1' },
    { code: '+44', flag: '🇬🇧', name: '+44' },
    { code: '+32', flag: '🇧🇪', name: '+32' },
    { code: '+49', flag: '🇩🇪', name: '+49' },
    { code: '+225', flag: '🇨🇮', name: '+225' },
    { code: '+221', flag: '🇸🇳', name: '+221' },
    { code: '+241', flag: '🇬🇦', name: '+241' },
    { code: '+242', flag: '🇨🇬', name: '+242' },
    { code: '+235', flag: '🇹🇩', name: '+235' },
  ];

  // Calculs réactifs
  readonly pin = computed(() => this.pinDigits().join(''));

  readonly isPinValid = computed(() => {
    return this.pin().length === 4 && /^\d{4}$/.test(this.pin());
  });

  readonly cleanPhone = computed(() => {
    return this.phoneNumber().replace(/\D/g, '');
  });

  readonly isPhoneValid = computed(() => {
    return this.cleanPhone().length >= 8 && this.cleanPhone().length <= 12;
  });

  readonly isFullNameValid = computed(() => {
    return this.fullName().trim().length >= 2;
  });

  readonly isFormValid = computed(() => {
    return (
      this.isFullNameValid() &&
      this.isPhoneValid() &&
      this.isPinValid() &&
      this.cguConsent()
    );
  });

  readonly isOtpValid = computed(() => {
    return this.otpCode().trim().length === 6;
  });

  readonly fullInternationalPhone = computed(() => {
    return `${this.countryCode()} ${this.phoneNumber()}`;
  });

  onFullNameInput(val: string): void {
    this.fullName.set(val);
    this.errorMessage.set('');
  }

  onCountryCodeChange(val: string): void {
    this.countryCode.set(val);
  }

  onPhoneInput(raw: string): void {
    let digits = raw.replace(/\D/g, '');
    if (digits.length > 9 && this.countryCode() === '+237') {
      digits = digits.substring(0, 9);
    }

    let formatted = '';
    if (digits.length > 0) {
      formatted = digits.substring(0, 3);
      if (digits.length > 3) {
        formatted += ' ' + digits.substring(3, 5);
      }
      if (digits.length > 5) {
        formatted += ' ' + digits.substring(5, 7);
      }
      if (digits.length > 7) {
        formatted += ' ' + digits.substring(7, 9);
      }
    }
    this.phoneNumber.set(formatted || digits);
    this.errorMessage.set('');
  }

  onCityInput(val: string): void {
    this.cityResidence.set(val);
  }

  onPinDigitInput(index: number, val: string): void {
    const clean = val.replace(/\D/g, '').slice(-1);
    const updated = [...this.pinDigits()];
    updated[index] = clean;
    this.pinDigits.set(updated);
    this.errorMessage.set('');
  }

  togglePinVisibility(): void {
    this.showPin.update((v) => !v);
  }

  onCguToggle(): void {
    this.cguConsent.update((v) => !v);
  }

  onOtpInput(raw: string): void {
    let digits = raw.replace(/\D/g, '');
    if (digits.length > 6) {
      digits = digits.substring(0, 6);
    }
    this.otpCode.set(digits);
    this.errorMessage.set('');
  }

  onSubmitRegister(): void {
    if (!this.isFormValid()) {
      if (!this.isFullNameValid()) {
        this.errorMessage.set('Veuillez renseigner votre nom complet.');
        return;
      }
      if (!this.isPhoneValid()) {
        this.errorMessage.set('Veuillez renseigner un numéro de téléphone valide.');
        return;
      }
      if (!this.isPinValid()) {
        this.errorMessage.set('Le code PIN doit comporter exactement 4 chiffres.');
        return;
      }
      if (!this.cguConsent()) {
        this.errorMessage.set('Veuillez accepter le règlement communautaire.');
        return;
      }
    }

    const fullPhone = `${this.countryCode()}${this.cleanPhone()}`;
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
      this.errorMessage.set('Code OTP invalide. Veuillez réessayer.');
    }
  }

  onSocialLogin(provider: 'Google' | 'Apple'): void {
    this.errorMessage.set('');
    this.authService.connexionSociale(provider === 'Google' ? 'GOOGLE' : 'FACEBOOK');
    this.router.navigate(['/tabs/dashboard']);
  }

  onRetour(): void {
    if (this.etape() === 'OTP') {
      this.etape.set('FORM');
      this.errorMessage.set('');
    } else {
      this.router.navigate(['/login']);
    }
  }

  onAllerConnexion(): void {
    this.router.navigate(['/login']);
  }
}
