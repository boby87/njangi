import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { StatusBadgeComponent } from '@shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-secretaire-view',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  templateUrl: './secretaire-view.component.html',
  styleUrl: './secretaire-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecretaireViewComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly reunion = this.tontineService.prochaineReunion;
  readonly reunions = this.tontineService.reunions;

  readonly compteRenduEnEdition = signal<string>(
    'Séance ouverte par le Président. Relevé des présences effectué par le Secrétaire. ' +
    'Quorum atteint. Les membres ont approuvé le compte-rendu précédent et procédé aux cotisations.'
  );

  onTexteChange(texte: string): void {
    this.compteRenduEnEdition.set(texte);
  }

  enregistrerPV(): void {
    const r = this.reunion();
    if (r) {
      this.tontineService.sauvegarderCompteRendu(r.id, this.compteRenduEnEdition(), false);
      alert('Compte-rendu de séance sauvegardé avec succès !');
    }
  }

  exporterPdf(): void {
    window.print();
  }
}
