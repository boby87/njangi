import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TontineStateService } from '@core/services/tontine-state.service';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';
import { AmountBadgeComponent } from '@shared/ui/amount-badge/amount-badge.component';
import { PenaltyModalComponent, SanctionFormValue } from '@shared/ui/penalty-modal/penalty-modal.component';

@Component({
  selector: 'app-penalite-list',
  standalone: true,
  imports: [CommonModule, StatusBadgeComponent, AmountBadgeComponent, PenaltyModalComponent],
  templateUrl: './penalite-list.component.html',
  styleUrl: './penalite-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PenaliteListComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly penalites = this.tontineService.penalites;
  readonly tarifications = this.tontineService.tarifications;
  readonly membres = this.tontineService.membres;

  readonly showModal = signal<boolean>(false);

  ouvrirModal(): void { this.showModal.set(true); }
  fermerModal(): void { this.showModal.set(false); }

  appliquerSanction(val: SanctionFormValue): void {
    this.tontineService.appliquerSanction(val);
    this.showModal.set(false);
  }

  reglerAmende(penaliteId: string): void {
    this.tontineService.reglerSanction(penaliteId);
    alert('Amende réglée avec succès et versée dans la caisse du groupe !');
  }
}
