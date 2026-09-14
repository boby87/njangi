import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonHeader,
  IonToolbar,
  IonContent,
  IonIcon
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  phonePortraitOutline,
  keyOutline,
  checkmarkCircle,
  shieldCheckmark,
  callOutline,
  arrowBackOutline
} from 'ionicons/icons';
import { MobileAuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader,
    IonToolbar,
    IonContent,
    IonIcon
  ],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  readonly authService = inject(MobileAuthService);
  private readonly router = inject(Router);

  // État Signal Forms pur (aucun FormGroup, aucun ngModel)
  readonly etape = signal<'PHONE' | 'OTP' | 'LIER_TELEPHONE'>('PHONE');
  readonly telephone = signal<string>('+237699123456');
  readonly telephoneLiaison = signal<string>('');
  readonly otpCode = signal<string>('');
  readonly errorMessage = signal<string>('');

  // Regex E.164 internationale : Cameroun (+237 ou 9 chiffres) + Diaspora (+33, +1, +49, etc.)
  private readonly phoneRegex = /^(\+[1-9][0-9]{6,14}|[236][0-9]{8})$/;

  readonly isPhoneValid = computed(() => {
    const clean = this.telephone().trim().replace(/\s+/g, '');
    return this.phoneRegex.test(clean);
  });

  readonly isPhoneLiaisonValid = computed(() => {
    const clean = this.telephoneLiaison().trim().replace(/\s+/g, '');
    return this.phoneRegex.test(clean);
  });

  readonly isOtpValid = computed(() => {
    return this.otpCode().trim().length === 6;
  });

  constructor() {
    addIcons({
      phonePortraitOutline,
      keyOutline,
      checkmarkCircle,
      shieldCheckmark,
      callOutline,
      arrowBackOutline
    });
  }

  onPhoneInput(val: string): void {
    this.telephone.set(val);
    this.errorMessage.set('');
  }

  onPhoneLiaisonInput(val: string): void {
    this.telephoneLiaison.set(val);
    this.errorMessage.set('');
  }

  onOtpInput(val: string): void {
    this.otpCode.set(val);
    this.errorMessage.set('');
  }

  onRequestOtp(): void {
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Format invalide. Indiquez un numéro valide (ex: +237 6XX ou +33 6XX).');
      return;
    }
    this.authService.requestOtp(this.telephone());
    this.otpCode.set(this.authService.simulatedOtp() || '884210');
    this.etape.set('OTP');
  }

  onVerifyOtp(): void {
    if (!this.isOtpValid()) {
      this.errorMessage.set('Le code OTP doit comporter 6 chiffres');
      return;
    }

    const ok = this.authService.verifyOtp(this.otpCode());
    if (ok) {
      this.router.navigate(['/tabs/dashboard']);
    } else {
      this.errorMessage.set('Code OTP invalide. Veuillez réessayer.');
    }
  }

  onSocialLogin(provider: 'GOOGLE' | 'FACEBOOK'): void {
    const res = this.authService.connexionSociale(provider);
    if (res.telephoneRequis) {
      this.etape.set('LIER_TELEPHONE');
    } else {
      this.router.navigate(['/tabs/dashboard']);
    }
  }

  onLierTelephone(): void {
    if (!this.isPhoneLiaisonValid()) {
      this.errorMessage.set('Format E.164 requis (ex: +237 6XX ou +33 6XX).');
      return;
    }
    this.authService.lierTelephone(this.telephoneLiaison());
    this.router.navigate(['/tabs/dashboard']);
  }

  onPasserLiaison(): void {
    this.router.navigate(['/tabs/dashboard']);
  }

  onRetourPhone(): void {
    this.authService.isAwaitingOtp.set(false);
    this.etape.set('PHONE');
    this.errorMessage.set('');
  }
}
