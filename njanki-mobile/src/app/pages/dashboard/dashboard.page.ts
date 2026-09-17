import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
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
  swapHorizontalOutline,
  personOutline,
  notificationsOutline,
  sparklesOutline,
  calendarOutline,
  arrowForwardOutline,
  receiptOutline,
  layersOutline,
  locationOutline,
  trendingUpOutline,
  fingerPrintOutline,
  checkmarkOutline,
  flashOutline,
  cardOutline
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
      swapHorizontalOutline,
      personOutline,
      notificationsOutline,
      sparklesOutline,
      calendarOutline,
      arrowForwardOutline,
      receiptOutline,
      layersOutline,
      locationOutline,
      trendingUpOutline,
      fingerPrintOutline,
      checkmarkOutline,
      flashOutline,
      cardOutline
    });
  }

  onRoleSelect(role: RoleMembre): void {
    this.authService.setActiveRole(role);
  }

  cleanAgendaItem(item: string): string {
    return (item || '').replace(/^\d+[\.\s]+/, '').trim();
  }

  getRoleIcon(role: RoleMembre): string {
    switch (role) {
      case 'PRESIDENT': return 'shield-checkmark-outline';
      case 'TRESORIER': return 'cash-outline';
      case 'SECRETAIRE': return 'document-text-outline';
      default: return 'person-outline';
    }
  }

  getRoleLabel(role: RoleMembre): string {
    switch (role) {
      case 'PRESIDENT': return 'Président';
      case 'TRESORIER': return 'Trésorier';
      case 'SECRETAIRE': return 'Secrétaire';
      default: return 'Membre';
    }
  }
}
