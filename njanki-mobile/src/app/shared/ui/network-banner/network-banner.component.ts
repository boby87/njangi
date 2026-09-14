import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonIcon } from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  cloudOfflineOutline,
  cellularOutline,
  syncOutline,
  checkmarkCircleOutline,
  alertCircleOutline
} from 'ionicons/icons';
import { NetworkService } from '../../../core/services/network.service';
import { MobileTontineStateService } from '../../../core/services/tontine-state.service';

@Component({
  selector: 'app-network-banner',
  standalone: true,
  imports: [CommonModule, IonIcon],
  templateUrl: './network-banner.component.html',
  styleUrl: './network-banner.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NetworkBannerComponent {
  readonly networkService = inject(NetworkService);
  readonly tontineState = inject(MobileTontineStateService);

  constructor() {
    addIcons({
      cloudOfflineOutline,
      cellularOutline,
      syncOutline,
      checkmarkCircleOutline,
      alertCircleOutline
    });
  }

  onSync(): void {
    this.tontineState.synchroniserActionsHorsLigne();
  }
}
