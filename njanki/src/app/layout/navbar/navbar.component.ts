import { Component, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { TontineStateService } from '@core/services/tontine-state.service';
import { RoleBadgeComponent } from '@shared/ui/role-badge/role-badge.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RoleBadgeComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent {
  readonly authService = inject(AuthService);
  readonly tontineService = inject(TontineStateService);

  readonly utilisateur = this.authService.utilisateur;
  readonly estModeDemo = this.authService.estModeDemo;
  readonly hasGroupes = this.tontineService.hasGroupes;
  readonly groupes = this.tontineService.groupes;
  readonly groupeActif = this.tontineService.groupeActif;
  readonly roleActif = this.tontineService.roleActif;
  readonly sessionActive = this.tontineService.sessionActive;

  readonly nomAffiche = computed<string>(() => {
    const u = this.utilisateur();
    const prenom = (u.prenom || '').trim();
    const nom = (u.nom || '').trim();
    const isProviderPlaceholder = /^(GOOGLE|FACEBOOK)$/i.test(prenom);
    if (isProviderPlaceholder) {
      return nom && !/^membre$/i.test(nom) ? nom : u.email || 'Membre Njangi';
    }
    const complet = `${prenom} ${nom}`.trim();
    return complet || u.email || u.telephone || 'Membre Njangi';
  });

  readonly sousTitreProfil = computed<string>(() => {
    const u = this.utilisateur();
    return u.telephone || u.email || 'Compte vérifié';
  });

  onChangerGroupe(event: Event): void {
    const select = event.target as HTMLSelectElement;
    if (select && select.value) {
      this.authService.setGroupeActif(select.value);
    }
  }

  onDeconnecter(): void {
    this.authService.deconnecter();
  }
}
