import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TontineStateService } from '@core/services/tontine-state.service';
import { AuthService } from '@core/services/auth.service';
import { RoleMembre } from '@shared/models/membre.model';
import { PresidentViewComponent } from './views/president-view.component';
import { TresorierViewComponent } from './views/tresorier-view.component';
import { SecretaireViewComponent } from './views/secretaire-view.component';
import { CreateurViewComponent } from './views/createur-view.component';
import { MembreViewComponent } from './views/membre-view.component';
import { RoleBadgeComponent } from '@shared/ui/role-badge/role-badge.component';

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

  readonly groupeActif = this.tontineService.groupeActif;
  readonly roleNaturel = this.tontineService.roleActif;

  // Rôle simulé pour permettre à l'utilisateur de tester facilement toutes les vues du cahier des charges
  readonly roleSimule = signal<RoleMembre | undefined>(undefined);

  readonly roleAffiche = computed<RoleMembre>(() => {
    return this.roleSimule() || this.roleNaturel();
  });

  simulerRole(role: RoleMembre): void {
    this.roleSimule.set(role);
  }

  reinitialiserRole(): void {
    this.roleSimule.set(undefined);
  }
}
