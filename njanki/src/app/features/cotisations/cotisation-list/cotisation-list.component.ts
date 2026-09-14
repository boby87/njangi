import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { TypeCotisation } from '@shared/models/cotisation.model';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';
import { AmountBadgeComponent } from '@shared/ui/amount-badge/amount-badge.component';

@Component({
  selector: 'app-cotisation-list',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, AmountBadgeComponent],
  templateUrl: './cotisation-list.component.html',
  styleUrl: './cotisation-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CotisationListComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly cotisations = this.tontineService.cotisations;

  // Filtre par type de cotisation (Multi-cotisations simultanées)
  readonly filtreType = signal<TypeCotisation | 'TOUS'>('TOUS');

  readonly cotisationsFiltrees = computed(() => {
    const f = this.filtreType();
    const list = this.cotisations().filter(c => c.groupeId === this.groupe().id);
    if (f === 'TOUS') return list;
    return list.filter(c => c.typeCotisation === f);
  });

  setFiltre(f: TypeCotisation | 'TOUS'): void {
    this.filtreType.set(f);
  }
}
