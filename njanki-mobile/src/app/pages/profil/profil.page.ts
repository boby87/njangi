import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonHeader,
  IonToolbar,
  IonContent,
  IonIcon
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  personCircleOutline,
  cellularOutline,
  syncOutline,
  logOutOutline,
  shieldCheckmarkOutline,
  refreshOutline,
  checkmarkCircle
} from 'ionicons/icons';
import { MobileAuthService } from '../../core/services/auth.service';
import { NetworkService } from '../../core/services/network.service';
import { MobileTontineStateService } from '../../core/services/tontine-state.service';
import { RoleBadgeComponent } from '../../shared/ui/role-badge/role-badge.component';

@Component({
  selector: 'app-profil',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader,
    IonToolbar,
    IonContent,
    IonIcon,
    RoleBadgeComponent
  ],
  templateUrl: './profil.page.html',
  styleUrl: './profil.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilPage {
  readonly authService = inject(MobileAuthService);
  readonly networkService = inject(NetworkService);
  readonly tontineState = inject(MobileTontineStateService);
  private readonly router = inject(Router);

  readonly isSyncing = signal<boolean>(false);
  readonly syncSuccess = signal<boolean>(false);

  constructor() {
    addIcons({
      personCircleOutline,
      cellularOutline,
      syncOutline,
      logOutOutline,
      shieldCheckmarkOutline,
      refreshOutline,
      checkmarkCircle
    });
  }

  onSyncOfflineQueue(): void {
    this.isSyncing.set(true);
    setTimeout(() => {
      this.tontineState.synchroniserActionsHorsLigne();
      this.isSyncing.set(false);
      this.syncSuccess.set(true);
      setTimeout(() => this.syncSuccess.set(false), 3000);
    }, 800);
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
