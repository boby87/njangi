import { Component, PLATFORM_ID, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
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
  readonly tontineService = inject(TontineStateService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly mandat = this.tontineService.mandatActif;
  readonly membres = this.tontineService.membresGroupeActif;

  readonly ongletActif = signal<'MANDAT' | 'SESSION' | 'MEMBRES'>('MANDAT');
  readonly codeCopie = signal<boolean>(false);

  setOnglet(onglet: 'MANDAT' | 'SESSION' | 'MEMBRES'): void {
    this.ongletActif.set(onglet);
  }

  copierCodeInvitation(): void {
    const code = this.groupe().codeInvitation;
    if (!code || !isPlatformBrowser(this.platformId)) {
      return;
    }
    if (navigator.clipboard) {
      navigator.clipboard.writeText(code);
      this.codeCopie.set(true);
      setTimeout(() => this.codeCopie.set(false), 2500);
    }
  }
}
