import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonHeader,
  IonToolbar,
  IonContent,
  IonIcon
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  checkmarkOutline,
  timeOutline,
  closeOutline,
  helpOutline,
  alertCircleOutline,
  megaphoneOutline
} from 'ionicons/icons';
import { MobileTontineStateService } from '../../core/services/tontine-state.service';
import { MobileAuthService } from '../../core/services/auth.service';
import { PresenceSeance } from '../../shared/models/reunion.model';
import { StatusBadgeComponent } from '../../shared/ui/status-badge/status-badge.component';
import { AmountBadgeComponent } from '../../shared/ui/amount-badge/amount-badge.component';
import { PenaltyModalComponent, SanctionFormValue } from '../../shared/ui/penalty-modal/penalty-modal.component';

@Component({
  selector: 'app-reunions',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader,
    IonToolbar,
    IonContent,
    IonIcon,
    StatusBadgeComponent,
    AmountBadgeComponent,
    PenaltyModalComponent
  ],
  templateUrl: './reunions.page.html',
  styleUrl: './reunions.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReunionsPage {
  readonly tontineState = inject(MobileTontineStateService);
  readonly authService = inject(MobileAuthService);

  readonly isSanctionModalOpen = signal<boolean>(false);

  constructor() {
    addIcons({
      checkmarkOutline,
      timeOutline,
      closeOutline,
      helpOutline,
      alertCircleOutline,
      megaphoneOutline
    });
  }

  onMarkPresence(membreId: string, statut: PresenceSeance['statut']): void {
    this.tontineState.marquerPresence(membreId, statut);
  }

  openSanctionModal(): void {
    this.isSanctionModalOpen.set(true);
  }

  closeSanctionModal(): void {
    this.isSanctionModalOpen.set(false);
  }

  onSubmitSanction(form: SanctionFormValue): void {
    this.tontineState.infligerSanction({
      groupeId: this.tontineState.activeGroupId(),
      reunionId: this.tontineState.activeReunion().id,
      membreId: form.membreId,
      typeInfraction: form.typeInfraction,
      montantXaf: form.montantXaf,
      motif: form.motif,
    });
    this.closeSanctionModal();
  }
}
