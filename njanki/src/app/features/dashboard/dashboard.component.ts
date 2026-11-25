import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
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

  readonly isDemoMode = !environment.production;
  readonly groupeActif = this.tontineService.groupeActif;
  readonly roleNaturel = this.tontineService.roleActif;

  // Rôle simulé uniquement en environnement de développement/démonstration
  readonly roleSimule = signal<RoleMembre | undefined>(undefined);

  readonly roleAffiche = computed<RoleMembre>(() => {
    return this.isDemoMode ? this.roleSimule() || this.roleNaturel() : this.roleNaturel();
  });

  simulerRole(role: RoleMembre): void {
    if (this.isDemoMode) {
      this.roleSimule.set(role);
    }
  }

  reinitialiserRole(): void {
    this.roleSimule.set(undefined);
  }
}
