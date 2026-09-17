import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { TontineStateService } from '@core/services/tontine-state.service';
import { RoleBadgeComponent } from '@shared/ui/role-badge/role-badge.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RoleBadgeComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent {
  private readonly router = inject(Router);
  readonly authService = inject(AuthService);
  readonly tontineService = inject(TontineStateService);

  readonly utilisateur = this.authService.utilisateur;
  readonly groupes = this.tontineService.groupes;
  readonly groupeActif = this.tontineService.groupeActif;
  readonly roleActif = this.tontineService.roleActif;
  readonly sessionActive = this.tontineService.sessionActive;

  onChangerGroupe(event: Event): void {
    const select = event.target as HTMLSelectElement;
    if (select && select.value) {
      this.authService.setGroupeActif(select.value);
    }
  }

  onDeconnecter(): void {
    this.authService.deconnecter();
    this.router.navigate(['/login']);
  }
}
