import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  IonTabs,
  IonTabBar,
  IonTabButton,
  IonIcon,
  IonLabel,
  IonBadge
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  homeOutline,
  walletOutline,
  calendarOutline,
  cardOutline,
  personOutline,
  cloudOfflineOutline,
  syncOutline
} from 'ionicons/icons';
import { NetworkService } from '../../core/services/network.service';
import { MobileTontineStateService } from '../../core/services/tontine-state.service';

import { NetworkBannerComponent } from '../../shared/ui/network-banner/network-banner.component';

@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    IonTabs,
    IonTabBar,
    IonTabButton,
    IonIcon,
    IonLabel,
    IonBadge,
    NetworkBannerComponent
  ],
  templateUrl: './tabs.page.html',
  styleUrl: './tabs.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TabsPage {
  readonly networkService = inject(NetworkService);
  readonly tontineState = inject(MobileTontineStateService);

  constructor() {
    addIcons({
      homeOutline,
      walletOutline,
      calendarOutline,
      cardOutline,
      personOutline,
      cloudOfflineOutline,
      syncOutline
    });
  }
}
