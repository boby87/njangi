import { Component, ChangeDetectionStrategy, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
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
import { RoleBadgeComponent } from '@shared/ui/role-badge/role-badge.component';

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

  readonly isSyncing = this.tontineState.isSyncing;
  readonly syncSuccess = computed(
    () =>
      !this.tontineState.isSyncing() &&
      !this.tontineState.syncError() &&
      this.tontineState.offlineQueue().length === 0 &&
      !!this.tontineState.lastSyncAt()
  );

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
    this.tontineState.synchroniserActionsHorsLigne();
  }

  onLogout(): void {
    this.authService.logout();
  }
}
