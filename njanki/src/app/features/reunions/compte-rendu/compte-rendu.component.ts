import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-compte-rendu',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './compte-rendu.component.html',
  styleUrl: './compte-rendu.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompteRenduComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly reunions = this.tontineService.reunions;

  imprimer(): void {
    window.print();
  }
}
