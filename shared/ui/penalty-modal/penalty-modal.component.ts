import { Component, input, output, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Membre } from '../../models/membre.model';
import { TypeInfraction } from '../../models/penalite.model';

export interface SanctionFormValue {
  membreId: string;
  typeInfraction: TypeInfraction;
  montantXaf: number;
  motif: string;
}

@Component({
  selector: 'app-penalty-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './penalty-modal.component.html',
  styleUrl: './penalty-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PenaltyModalComponent {
  readonly membres = input.required<Membre[]>();
  readonly close = output<void>();
  readonly submitSanction = output<SanctionFormValue>();

  // État du formulaire géré par Signals (Signal Forms pur, aucun FormGroup/ngModel)
  readonly membreId = signal<string>('');
  readonly typeInfraction = signal<TypeInfraction>('RETARD_REUNION');
  readonly montantXaf = signal<number>(1000);
  readonly motif = signal<string>('');

  // Barème par défaut en XAF selon le type d'infraction
  readonly baremeParDefaut: Record<TypeInfraction, number> = {
    RETARD_REUNION: 1000,
    BAVARDAGE_SONNERIE: 500,
    ABSENCE_NON_JUSTIFIEE: 5000,
    RETARD_PAIEMENT: 2000,
    NON_RESPECT_REGLE: 2500,
    AUTRE: 1000,
  };

  // Validation réactive avec computed()
  readonly isValid = computed(() => {
    return this.membreId().trim().length > 0 &&
           this.montantXaf() > 0 &&
           this.motif().trim().length >= 3;
  });

  onMembreChange(id: string): void {
    this.membreId.set(id);
  }

  onTypeInfractionChange(val: string): void {
    const inf = val as TypeInfraction;
    this.typeInfraction.set(inf);
    this.montantXaf.set(this.baremeParDefaut[inf] || 1000);
    if (!this.motif()) {
      switch (inf) {
        case 'RETARD_REUNION': this.motif.set('Arrivée tardive à la séance'); break;
        case 'BAVARDAGE_SONNERIE': this.motif.set('Sonnerie de téléphone / bavardage pendant l\'ordre du jour'); break;
        case 'ABSENCE_NON_JUSTIFIEE': this.motif.set('Absence à la réunion sans mot d\'excuse préalable'); break;
        case 'RETARD_PAIEMENT': this.motif.set('Dépassement de la date d\'échéance de cotisation'); break;
        case 'NON_RESPECT_REGLE': this.motif.set('Manquement au règlement intérieur du groupe'); break;
        default: this.motif.set(''); break;
      }
    }
  }

  onMontantChange(val: string): void {
    const parsed = parseInt(val, 10);
    this.montantXaf.set(isNaN(parsed) ? 0 : parsed);
  }

  onMotifChange(val: string): void {
    this.motif.set(val);
  }

  onSubmit(): void {
    if (!this.isValid()) return;
    this.submitSanction.emit({
      membreId: this.membreId(),
      typeInfraction: this.typeInfraction(),
      montantXaf: this.montantXaf(),
      motif: this.motif(),
    });
  }

  onClose(): void {
    this.close.emit();
  }
}
