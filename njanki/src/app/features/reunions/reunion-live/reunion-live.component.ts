import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { StatutPresence } from '@shared/models/reunion.model';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';
import { PenaltyModalComponent, SanctionFormValue } from '@shared/ui/penalty-modal/penalty-modal.component';

@Component({
  selector: 'app-reunion-live',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, PenaltyModalComponent],
  templateUrl: './reunion-live.component.html',
  styleUrl: './reunion-live.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReunionLiveComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly reunion = this.tontineService.prochaineReunion;
  readonly membres = this.tontineService.membres;

  readonly showPenaltyModal = signal<boolean>(false);
  readonly compteRenduTexte = signal<string>(
    'La séance s\'est ouverte à l\'heure prévue sous la présidence de M. Mbarga. ' +
    'Le secrétaire a procédé à l\'appel nominal. Les cotisations du jour ont été collectées avec rigueur.'
  );

  marquerPresence(membreId: string, statut: StatutPresence): void {
    const r = this.reunion();
    if (r) {
      this.tontineService.mettreAJourPresence(r.id, membreId, statut);
    }
  }

  ouvrirSanction(): void {
    this.showPenaltyModal.set(true);
  }

  fermerSanction(): void {
    this.showPenaltyModal.set(false);
  }

  appliquerSanction(val: SanctionFormValue): void {
    this.tontineService.appliquerSanction(val);
    this.showPenaltyModal.set(false);
  }

  cloturerSeance(): void {
    const r = this.reunion();
    if (r) {
      this.tontineService.sauvegarderCompteRendu(r.id, this.compteRenduTexte(), true);
      alert('Séance clôturée avec succès ! Le procès-verbal officiel a été généré.');
    }
  }
}
