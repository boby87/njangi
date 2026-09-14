import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { AmountBadgeComponent } from '@shared/ui/amount-badge/amount-badge.component';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';
import { ProofPreviewComponent } from '@shared/ui/proof-preview/proof-preview.component';

@Component({
  selector: 'app-tresorier-view',
  standalone: true,
  imports: [CommonModule, RouterLink, AmountBadgeComponent, StatusBadgeComponent, ProofPreviewComponent],
  templateUrl: './tresorier-view.component.html',
  styleUrl: './tresorier-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TresorierViewComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly stats = this.tontineService.statsGroupeActif;
  readonly cotisations = this.tontineService.cotisations;
  readonly paiements = this.tontineService.paiements;
  readonly potEnCours = this.tontineService.potEnCours;

  // Prévisualisation de reçu signed
  readonly previewRecuUrl = signal<string | undefined>(undefined);

  voirPreuve(url?: string): void {
    this.previewRecuUrl.set(url);
  }

  fermerPreuve(): void {
    this.previewRecuUrl.set(undefined);
  }

  decaisserPot(): void {
    const pot = this.potEnCours();
    if (pot) {
      this.tontineService.decaisserPot(pot.id, 'Mobile Money (MTN / Orange)');
    }
  }

  relancerMembre(nom: string): void {
    alert(`Rappel SMS envoyé avec succès au membre ${nom} avec lien de paiement Mobile Money.`);
  }
}
