import { Component, DestroyRef, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProblemDetail } from '@shared/models/api-response.model';
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
  private readonly destroyRef = inject(DestroyRef);

  // État du formulaire en Signal Forms purs
  readonly telephoneOuEmail = signal<string>('+237699123456');
  readonly codeOtp = signal<string>('');
  readonly etape = signal<'IDENTIFIANT' | 'OTP' | 'LIER_TELEPHONE'>('IDENTIFIANT');
  readonly messageErreur = signal<string>('');
  readonly chargement = signal<boolean>(false);

  // Authentification Sociale (Google & Facebook)
  readonly isSocialLoading = signal<'GOOGLE' | 'FACEBOOK' | null>(null);
  readonly telephoneLiaison = signal<string>('+237699123456');
  readonly codeOtpLiaison = signal<string>('');

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
          this.codeOtp.set('');
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

  connexionSociale(provider: 'GOOGLE' | 'FACEBOOK'): void {
    this.messageErreur.set('');
    this.isSocialLoading.set(provider);

    this.authService
      .connexionSociale(provider)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultat) => {
          this.isSocialLoading.set(null);
          if (!resultat.succes) {
            this.messageErreur.set(`Échec de la connexion avec ${provider}.`);
            return;
          }
          if (resultat.telephoneRequis) {
            this.etape.set('LIER_TELEPHONE');
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err: unknown) => {
          this.isSocialLoading.set(null);
          this.messageErreur.set(
            this.extraireMessageErreur(err, `Impossible de se connecter via ${provider}.`)
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

    this.messageErreur.set('');
    this.chargement.set(true);

    this.authService
      .lierTelephone(this.telephoneLiaison().trim(), this.codeOtpLiaison().trim() || undefined)
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
    // Permet d'accéder au tableau de bord en mode consultation
    this.router.navigate(['/dashboard']);
  }

  retourIdentifiant(): void {
    this.etape.set('IDENTIFIANT');
    this.messageErreur.set('');
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
