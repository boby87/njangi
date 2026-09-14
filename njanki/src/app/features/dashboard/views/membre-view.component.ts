import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { AmountBadgeComponent } from '@shared/ui/amount-badge/amount-badge.component';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-membre-view',
  standalone: true,
  imports: [CommonModule, RouterLink, AmountBadgeComponent, StatusBadgeComponent],
  templateUrl: './membre-view.component.html',
  styleUrl: './membre-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MembreViewComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly mesCotisations = this.tontineService.mesCotisations;
  readonly monTourPot = this.tontineService.monTourPot;
  readonly potEnCours = this.tontineService.potEnCours;
  readonly stats = this.tontineService.statsGroupeActif;
  readonly prochaineReunion = this.tontineService.prochaineReunion;
}
