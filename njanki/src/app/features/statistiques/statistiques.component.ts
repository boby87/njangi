import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TontineStateService } from '@core/services/tontine-state.service';
@Component({
  selector: 'app-statistiques',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistiques.component.html',
  styleUrl: './statistiques.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatistiquesComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly stats = this.tontineService.statsGroupeActif;
  readonly cotisations = this.tontineService.cotisations;

  exporterRapport(): void {
    window.print();
  }
}
