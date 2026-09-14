import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { AuthService } from '@core/services/auth.service';
import { RoleBadgeComponent } from '@shared/ui/role-badge/role-badge.component';

@Component({
  selector: 'app-groupe-list',
  standalone: true,
  imports: [CommonModule, RouterLink, RoleBadgeComponent],
  templateUrl: './groupe-list.component.html',
  styleUrl: './groupe-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupeListComponent {
  readonly tontineService = inject(TontineStateService);
  readonly authService = inject(AuthService);

  readonly groupes = this.tontineService.groupes;
  readonly adhesions = this.authService.adhesions;
  readonly groupeActif = this.tontineService.groupeActif;

  getMonRole(groupeId: string): import('@shared/models/membre.model').RoleMembre {
    const adh = this.adhesions().find(a => a.groupeId === groupeId);
    return adh?.role || 'MEMBRE';
  }

  selectionnerGroupe(groupeId: string): void {
    this.authService.setGroupeActif(groupeId);
  }
}
