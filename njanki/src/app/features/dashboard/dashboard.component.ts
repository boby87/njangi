import { Component, DestroyRef, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TontineStateService } from '@core/services/tontine-state.service';
import { AuthService } from '@core/services/auth.service';
import { RoleMembre } from '@shared/models/membre.model';
import { environment } from '../../../environments/environment';
import { PresidentViewComponent } from './views/president-view.component';
import { TresorierViewComponent } from './views/tresorier-view.component';
import { SecretaireViewComponent } from './views/secretaire-view.component';
import { CreateurViewComponent } from './views/createur-view.component';
import { MembreViewComponent } from './views/membre-view.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    PresidentViewComponent,
    TresorierViewComponent,
    SecretaireViewComponent,
    CreateurViewComponent,
    MembreViewComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  readonly tontineService = inject(TontineStateService);
  readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly isDemoMode = !environment.production;
  readonly hasGroupes = this.tontineService.hasGroupes;
  readonly isLoading = this.tontineService.isLoading;
  readonly utilisateur = this.authService.utilisateur;
  readonly groupeActif = this.tontineService.groupeActif;
  readonly roleNaturel = this.tontineService.roleActif;

  // Signal Form d'onboarding pour rejoindre une tontine via code d'invitation
  readonly codeInvitation = signal<string>('');
  readonly joinLoading = signal<boolean>(false);
  readonly joinError = signal<string>('');
  readonly joinSuccess = signal<string>('');
  readonly isCodeValide = computed<boolean>(() => this.codeInvitation().trim().length >= 4);

  // Rôle simulé uniquement en environnement de développement/démonstration
  readonly roleSimule = signal<RoleMembre | undefined>(undefined);

  readonly roleAffiche = computed<RoleMembre>(() => {
    return this.isDemoMode ? this.roleSimule() || this.roleNaturel() : this.roleNaturel();
  });

  onCodeInvitationInput(val: string): void {
    this.codeInvitation.set(val);
    this.joinError.set('');
    this.joinSuccess.set('');
  }

  rejoindreParCode(): void {
    if (!this.isCodeValide() || this.joinLoading()) {
      return;
    }

    this.joinLoading.set(true);
    this.joinError.set('');
    this.joinSuccess.set('');

    this.tontineService
      .rejoindreGroupeParCode(this.codeInvitation())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        this.joinLoading.set(false);
        if (res.succes) {
          this.joinSuccess.set(res.message);
          this.codeInvitation.set('');
        } else {
          this.joinError.set(res.message);
        }
      });
  }

  lancerDemo(): void {
    if (this.isDemoMode) {
      this.authService.connexionDemo();
    }
  }

  simulerRole(role: RoleMembre): void {
    if (this.isDemoMode) {
      this.roleSimule.set(role);
    }
  }

  reinitialiserRole(): void {
    this.roleSimule.set(undefined);
  }
}
