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
  walletOutline,
  heartOutline,
  shieldCheckmarkOutline,
  checkmarkCircle,
  timeOutline,
  personOutline
} from 'ionicons/icons';
import { MobileTontineStateService } from '../../core/services/tontine-state.service';
import { TypeCotisation } from '../../shared/models/cotisation.model';
import { AmountBadgeComponent } from '../../shared/ui/amount-badge/amount-badge.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-cotisations',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader,
    IonToolbar,
    IonContent,
    IonIcon,
    AmountBadgeComponent,
    StatusBadgeComponent
  ],
  templateUrl: './cotisations.page.html',
  styleUrl: './cotisations.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CotisationsPage {
  readonly tontineState = inject(MobileTontineStateService);

  readonly selectedType = signal<TypeCotisation | 'TOUS'>('TOUS');

  readonly filteredCotisations = computed(() => {
    const type = this.selectedType();
    if (type === 'TOUS') {
      return this.tontineState.cotisations();
    }
    return this.tontineState.cotisations().filter((c) => c.typeCotisation === type);
  });

  constructor() {
    addIcons({
      walletOutline,
      heartOutline,
      shieldCheckmarkOutline,
      checkmarkCircle,
      timeOutline,
      personOutline
    });
  }

  onTypeChange(val: string): void {
    this.selectedType.set(val as TypeCotisation | 'TOUS');
  }
}
