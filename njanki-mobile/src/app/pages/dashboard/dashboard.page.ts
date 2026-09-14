import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  IonHeader,
  IonToolbar,
  IonContent,
  IonIcon
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  cashOutline,
  shieldCheckmarkOutline,
  peopleOutline,
  timeOutline,
  checkmarkCircle,
  alertCircle,
  chevronForwardOutline,
  walletOutline,
  documentTextOutline,
  ribbonOutline,
  swapHorizontalOutline
} from 'ionicons/icons';
import { MobileTontineStateService } from '../../core/services/tontine-state.service';
import { MobileAuthService } from '../../core/services/auth.service';
import { RoleMembre } from '../../shared/models/membre.model';
import { AmountBadgeComponent } from '../../shared/ui/amount-badge/amount-badge.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge/status-badge.component';
import { RoleBadgeComponent } from '../../shared/ui/role-badge/role-badge.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    IonHeader,
    IonToolbar,
    IonContent,
    IonIcon,
    AmountBadgeComponent,
    StatusBadgeComponent,
    RoleBadgeComponent
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardPage {
  readonly tontineState = inject(MobileTontineStateService);
  readonly authService = inject(MobileAuthService);

  readonly availableRoles: RoleMembre[] = ['MEMBRE', 'TRESORIER', 'PRESIDENT', 'SECRETAIRE'];

  constructor() {
    addIcons({
      cashOutline,
      shieldCheckmarkOutline,
      peopleOutline,
      timeOutline,
      checkmarkCircle,
      alertCircle,
      chevronForwardOutline,
      walletOutline,
      documentTextOutline,
      ribbonOutline,
      swapHorizontalOutline
    });
  }

  onRoleSelect(role: RoleMembre): void {
    this.authService.setActiveRole(role);
  }
}
