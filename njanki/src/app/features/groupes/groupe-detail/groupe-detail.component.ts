import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { RoleBadgeComponent } from '@shared/ui/role-badge/role-badge.component';

@Component({
  selector: 'app-groupe-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, RoleBadgeComponent],
  templateUrl: './groupe-detail.component.html',
  styleUrl: './groupe-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupeDetailComponent {
  private readonly route = inject(ActivatedRoute);
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly mandat = this.tontineService.mandatActif;
  readonly membres = this.tontineService.membres;

  readonly ongletActif = signal<'MANDAT' | 'SESSION' | 'MEMBRES'>('MANDAT');

  setOnglet(onglet: 'MANDAT' | 'SESSION' | 'MEMBRES'): void {
    this.ongletActif.set(onglet);
  }
}
