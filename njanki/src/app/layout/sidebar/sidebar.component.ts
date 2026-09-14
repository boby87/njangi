import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarComponent {
  readonly tontineService = inject(TontineStateService);
  readonly roleActif = this.tontineService.roleActif;
  readonly groupeActif = this.tontineService.groupeActif;
  readonly stats = this.tontineService.statsGroupeActif;
}
