import { Component, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // État du formulaire en Signal Forms purs (zéro FormGroup/ngModel)
  readonly telephoneOuEmail = signal<string>('+237699123456');
  readonly codeOtp = signal<string>('');
  readonly etape = signal<'IDENTIFIANT' | 'OTP' | 'LIER_TELEPHONE'>('IDENTIFIANT');
  readonly messageErreur = signal<string>('');
  readonly chargement = signal<boolean>(false);

  // Authentification Sociale (Google & Facebook)
  readonly isSocialLoading = signal<'GOOGLE' | 'FACEBOOK' | null>(null);
  readonly telephoneLiaison = signal<string>('+237699123456');
  readonly codeOtpLiaison = signal<string>('123456');

  // Validation réactive avec computed()
  readonly isIdentifiantValide = computed(() => {
    const val = this.telephoneOuEmail().trim();
    return (val.startsWith('+') && val.length >= 9) || (val.length === 9) || val.includes('@');
  });

  readonly isOtpValide = computed(() => {
    return this.codeOtp().trim().length === 6;
  });

  readonly isTelephoneLiaisonValide = computed(() => {
    const val = this.telephoneLiaison().trim();
    return (val.startsWith('+') && val.length >= 9) || (val.length === 9);
  });

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
  }

  demanderOtp(): void {
    if (!this.isIdentifiantValide()) {
      this.messageErreur.set('Veuillez saisir un numéro de téléphone international (+237..., +33...) ou un email valide.');
      return;
    }
    this.chargement.set(true);
    this.authService.demanderOtp(this.telephoneOuEmail());
    this.etape.set('OTP');
    this.codeOtp.set('123456'); // Code de démonstration pré-rempli
    this.chargement.set(false);
  }

  validerOtp(): void {
    if (!this.isOtpValide()) {
      this.messageErreur.set('Le code OTP doit comporter 6 chiffres.');
      return;
    }
    this.chargement.set(true);
    const succes = this.authService.verifierOtp(this.codeOtp());
    this.chargement.set(false);

    if (succes) {
      this.router.navigate(['/dashboard']);
    } else {
      this.messageErreur.set('Code OTP incorrect. Veuillez utiliser le code de test 123456.');
    }
  }

  connexionSociale(provider: 'GOOGLE' | 'FACEBOOK'): void {
    this.messageErreur.set('');
    this.isSocialLoading.set(provider);

    // Simulation de flux OAuth2 OpenID Connect (sans Promise)
    const resultat = this.authService.connexionSociale(provider);
    this.isSocialLoading.set(null);

    if (resultat.telephoneRequis) {
      this.etape.set('LIER_TELEPHONE');
    } else {
      this.router.navigate(['/dashboard']);
    }
  }

  confirmerLiaisonTelephone(): void {
    if (!this.isTelephoneLiaisonValide()) {
      this.messageErreur.set('Veuillez renseigner un numéro de téléphone valide (ex: +237699123456 ou +33612345678).');
      return;
    }
    this.chargement.set(true);
    this.authService.lierTelephone(this.telephoneLiaison());
    this.chargement.set(false);
    this.router.navigate(['/dashboard']);
  }

  ignorerLiaisonTelephone(): void {
    // Permet d'accéder au tableau de bord en mode consultation
    this.router.navigate(['/dashboard']);
  }

  retourIdentifiant(): void {
    this.etape.set('IDENTIFIANT');
    this.messageErreur.set('');
  }
}
