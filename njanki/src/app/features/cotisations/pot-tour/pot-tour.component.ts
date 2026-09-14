import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-pot-tour',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './pot-tour.component.html',
  styleUrl: './pot-tour.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PotTourComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly toursPot = this.tontineService.toursPot;
  readonly roleActif = this.tontineService.roleActif;

  decaisser(tourId: string): void {
    this.tontineService.decaisserPot(tourId, 'MTN Mobile Money');
    alert('Versement du pot effectué avec succès par Mobile Money ! Quittance numérique générée.');
  }

  tirageAuSort(): void {
    alert('Tirage au sort automatique exécuté avec succès en présence des membres. L\'ordre de passage a été mis à jour.');
  }
}
