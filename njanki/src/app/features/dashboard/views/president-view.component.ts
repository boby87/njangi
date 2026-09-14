import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';
import { PenaltyModalComponent, SanctionFormValue } from '@shared/ui/penalty-modal/penalty-modal.component';

@Component({
  selector: 'app-president-view',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, PenaltyModalComponent],
  templateUrl: './president-view.component.html',
  styleUrl: './president-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PresidentViewComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly mandat = this.tontineService.mandatActif;
  readonly stats = this.tontineService.statsGroupeActif;
  readonly membres = this.tontineService.membres;
  readonly prochaineReunion = this.tontineService.prochaineReunion;
  readonly potEnCours = this.tontineService.potEnCours;
  readonly penalites = this.tontineService.penalites;

  readonly showPenaltyModal = signal<boolean>(false);

  ouvrirModalSanction(): void {
    this.showPenaltyModal.set(true);
  }

  fermerModalSanction(): void {
    this.showPenaltyModal.set(false);
  }

  appliquerSanction(val: SanctionFormValue): void {
    this.tontineService.appliquerSanction(val);
    this.showPenaltyModal.set(false);
  }
}
