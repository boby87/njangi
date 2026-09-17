import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
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
  private readonly router = inject(Router);
  readonly authService = inject(AuthService);
  readonly tontineService = inject(TontineStateService);
  readonly roleActif = this.tontineService.roleActif;
  readonly groupeActif = this.tontineService.groupeActif;
  readonly stats = this.tontineService.statsGroupeActif;

  onDeconnecter(): void {
    this.authService.deconnecter();
    this.router.navigate(['/login']);
  }
}
