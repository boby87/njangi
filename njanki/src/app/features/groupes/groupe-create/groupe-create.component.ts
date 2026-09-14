import { Component, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { TypeSiege, FrequenceReunion } from '@shared/models/groupe.model';

@Component({
  selector: 'app-groupe-create',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './groupe-create.component.html',
  styleUrl: './groupe-create.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupeCreateComponent {
  private readonly tontineService = inject(TontineStateService);
  private readonly router = inject(Router);

  // État du formulaire via Signals purs (Signal Forms)
  readonly nom = signal<string>('');
  readonly description = signal<string>('');
  readonly typeSiege = signal<TypeSiege>('ROTATIF');
  readonly adresseSiegeFixe = signal<string>('');
  readonly frequenceReunion = signal<FrequenceReunion>('MENSUELLE');
  readonly nombreMembresMax = signal<number>(12);
  readonly montantPotTourXaf = signal<number>(50000);
  readonly montantSecoursXaf = signal<number>(5000);
  readonly montantReserveXaf = signal<number>(2000);

  // Validation réactive avec computed()
  readonly isValid = computed(() => {
    const nomOk = this.nom().trim().length >= 3;
    const descOk = this.description().trim().length >= 5;
    const membresOk = this.nombreMembresMax() >= 3 && this.nombreMembresMax() <= 50;
    const potOk = this.montantPotTourXaf() >= 1000;
    const siegeOk = this.typeSiege() === 'ROTATIF' || (this.typeSiege() === 'FIXE' && this.adresseSiegeFixe().trim().length >= 3);
    return nomOk && descOk && membresOk && potOk && siegeOk;
  });

  onNomInput(val: string): void { this.nom.set(val); }
  onDescriptionInput(val: string): void { this.description.set(val); }
  onTypeSiegeChange(val: string): void { this.typeSiege.set(val as TypeSiege); }
  onAdresseInput(val: string): void { this.adresseSiegeFixe.set(val); }
  onFrequenceChange(val: string): void { this.frequenceReunion.set(val as FrequenceReunion); }
  onNombreMembresInput(val: string): void { this.nombreMembresMax.set(parseInt(val, 10) || 0); }
  onMontantPotInput(val: string): void { this.montantPotTourXaf.set(parseInt(val, 10) || 0); }
  onMontantSecoursInput(val: string): void { this.montantSecoursXaf.set(parseInt(val, 10) || 0); }
  onMontantReserveInput(val: string): void { this.montantReserveXaf.set(parseInt(val, 10) || 0); }

  onSubmit(): void {
    if (!this.isValid()) return;

    const nouveauId = this.tontineService.creerGroupe({
      nom: this.nom(),
      description: this.description(),
      typeSiege: this.typeSiege(),
      adresseSiegeFixe: this.typeSiege() === 'FIXE' ? this.adresseSiegeFixe() : undefined,
      frequenceReunion: this.frequenceReunion(),
      nombreMembresMax: this.nombreMembresMax(),
      montantPotTourXaf: this.montantPotTourXaf(),
    });

    this.router.navigate(['/groupes', nouveauId]);
  }

  onAnnuler(): void {
    this.router.navigate(['/groupes']);
  }
}
