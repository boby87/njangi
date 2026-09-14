import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonHeader,
  IonToolbar,
  IonContent,
  IonIcon
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  cashOutline,
  phonePortraitOutline,
  cloudUploadOutline,
  checkmarkCircle,
  eyeOutline,
  receiptOutline
} from 'ionicons/icons';
import { MobileTontineStateService } from '../../core/services/tontine-state.service';
import { MobileAuthService } from '../../core/services/auth.service';
import { ModePaiement } from '../../shared/models/paiement.model';
import { AmountBadgeComponent } from '../../shared/ui/amount-badge/amount-badge.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge/status-badge.component';
import { ProofPreviewComponent } from '../../shared/ui/proof-preview/proof-preview.component';

@Component({
  selector: 'app-paiements',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader,
    IonToolbar,
    IonContent,
    IonIcon,
    AmountBadgeComponent,
    StatusBadgeComponent,
    ProofPreviewComponent
  ],
  templateUrl: './paiements.page.html',
  styleUrl: './paiements.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaiementsPage {
  readonly tontineState = inject(MobileTontineStateService);
  readonly authService = inject(MobileAuthService);

  // État Signal Forms pur (aucun FormGroup, aucun ngModel)
  readonly membreId = signal<string>('m-001');
  readonly modePaiement = signal<ModePaiement>('CASH');
  readonly montantXaf = signal<number>(50000);
  readonly recuUrl = signal<string>('https://images.unsplash.com/photo-1554415707-9e4966a604f7?w=600&q=80');
  readonly telephoneMoMo = signal<string>('+237677123456');

  readonly previewingProofUrl = signal<string | null>(null);
  readonly showSuccessToast = signal<boolean>(false);

  // Validation réactive avec computed()
  readonly isValid = computed(() => {
    if (this.montantXaf() <= 0) return false;
    if (!this.membreId()) return false;
    if (this.modePaiement() === 'CASH') {
      // Reçu physique obligatoire pour tout paiement cash
      return this.recuUrl().trim().length > 0;
    }
    if (this.modePaiement() === 'MTN_MOMO' || this.modePaiement() === 'ORANGE_MONEY') {
      return this.telephoneMoMo().trim().length >= 9;
    }
    return true;
  });

  constructor() {
    addIcons({
      cashOutline,
      phonePortraitOutline,
      cloudUploadOutline,
      checkmarkCircle,
      eyeOutline,
      receiptOutline
    });
  }

  onModeSelect(mode: ModePaiement): void {
    this.modePaiement.set(mode);
  }

  onMembreChange(id: string): void {
    this.membreId.set(id);
  }

  onMontantChange(val: string): void {
    const num = parseInt(val, 10);
    this.montantXaf.set(isNaN(num) ? 0 : num);
  }

  onPhoneChange(val: string): void {
    this.telephoneMoMo.set(val);
  }

  onUploadSimulatedReceipt(): void {
    // Simulation upload reçu photo signé physique
    this.recuUrl.set('https://images.unsplash.com/photo-1554415707-9e4966a604f7?w=600&q=80');
  }

  onOpenProof(url?: string): void {
    this.previewingProofUrl.set(url || this.recuUrl());
  }

  onCloseProof(): void {
    this.previewingProofUrl.set(null);
  }

  onSubmit(): void {
    if (!this.isValid()) return;

    this.tontineState.enregistrerPaiement({
      cotisationId: 'cot-001',
      groupeId: this.tontineState.activeGroupId(),
      sessionId: this.tontineState.activeSession().id,
      membreId: this.membreId(),
      membreNom: 'Jean-Paul FOKOU',
      montantXaf: this.montantXaf(),
      modePaiement: this.modePaiement(),
      typeCotisation: 'ROTATIVE_POT',
      valideParTresorierId: 'm-003',
      preuveUrl: this.modePaiement() === 'CASH' ? this.recuUrl() : undefined,
      referenceTransaction: this.modePaiement() !== 'CASH' ? 'MOMO-TX-' + Math.floor(100000 + Math.random() * 900000) : undefined,
    });

    this.showSuccessToast.set(true);
    setTimeout(() => this.showSuccessToast.set(false), 3000);
  }
}
