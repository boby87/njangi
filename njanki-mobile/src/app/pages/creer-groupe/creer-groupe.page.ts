import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { IonContent } from '@ionic/angular';

@Component({
  selector: 'app-creer-groupe',
  standalone: true,
  imports: [CommonModule, IonContent],
  templateUrl: './creer-groupe.page.html',
  styleUrl: './creer-groupe.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreerGroupePage {
  private readonly router = inject(Router);

  // État Signal Forms pur
  readonly nomGroupe = signal<string>('Solidarité Bonapriso Yaoundé');
  readonly description = signal<string>(
    'Épargne rotative mensuelle, fonds d\'entraide familiale et consolidation de projets d\'investissement.'
  );
  readonly langue = signal<'fr' | 'en' | 'loc'>('fr');

  // Paramétrage financier
  readonly miseUnitaire = signal<number>(50000);
  readonly frequence = signal<'hebdo' | 'mensuel' | 'bimensuel' | 'custom'>('mensuel');
  readonly nombreMembres = signal<number>(12);

  // Type de siège (Critère F-03 Obligatoire)
  readonly typeSiege = signal<'FIXE' | 'ROTATIF'>('ROTATIF');
  readonly adresseSiegeFixe = signal<string>('Foyer Bandjoun, Akwa, Douala');
  readonly methodeRotation = signal<'tirage' | 'alphabetique' | 'accord'>('tirage');

  // Règles coutumières & sanctions
  readonly amendeRetard = signal<number>(2000);
  readonly amendeAbsence = signal<number>(5000);
  readonly caisseSecours = signal<number>(5000);

  readonly errorMessage = signal<string>('');
  readonly successMessage = signal<string>('');
  readonly isSubmitting = signal<boolean>(false);

  // Calculs réactifs (computed)
  readonly totalPot = computed(() => this.miseUnitaire() * this.nombreMembres());

  readonly totalPotFormatted = computed(() => {
    return this.totalPot().toLocaleString('fr-FR') + ' FCFA';
  });

  readonly diasporaAmountEur = computed(() => {
    return Math.round((this.miseUnitaire() / 655.957) * 100) / 100;
  });

  readonly cycleDuration = computed(() => {
    const count = this.nombreMembres();
    switch (this.frequence()) {
      case 'hebdo':
        return `${count} semaines`;
      case 'mensuel':
        return `${count} mois`;
      case 'bimensuel':
        return `${Math.ceil(count / 2)} mois`;
      case 'custom':
        return `${count} séances`;
    }
  });

  readonly isFormValid = computed(() => {
    const nomOk = this.nomGroupe().trim().length >= 3;
    const miseOk = this.miseUnitaire() >= 5000;
    const membresOk = this.nombreMembres() >= 3;
    const siegeOk =
      this.typeSiege() === 'ROTATIF' || this.adresseSiegeFixe().trim().length >= 3;
    return nomOk && miseOk && membresOk && siegeOk;
  });

  onNomInput(val: string): void {
    this.nomGroupe.set(val);
    this.errorMessage.set('');
  }

  onDescInput(val: string): void {
    this.description.set(val);
  }

  setLangue(l: 'fr' | 'en' | 'loc'): void {
    this.langue.set(l);
  }

  setMiseUnitaire(montant: number): void {
    this.miseUnitaire.set(montant);
  }

  onMiseInputChange(val: string): void {
    const num = parseInt(val.replace(/\D/g, ''), 10);
    if (!isNaN(num)) {
      this.miseUnitaire.set(num);
    }
  }

  setFrequence(f: 'hebdo' | 'mensuel' | 'bimensuel' | 'custom'): void {
    this.frequence.set(f);
  }

  incrementMembres(): void {
    if (this.nombreMembres() < 36) {
      this.nombreMembres.update((n) => n + 1);
    }
  }

  decrementMembres(): void {
    if (this.nombreMembres() > 3) {
      this.nombreMembres.update((n) => n - 1);
    }
  }

  onMembresRangeChange(val: string): void {
    const n = parseInt(val, 10);
    if (!isNaN(n) && n >= 3 && n <= 36) {
      this.nombreMembres.set(n);
    }
  }

  setTypeSiege(t: 'FIXE' | 'ROTATIF'): void {
    this.typeSiege.set(t);
  }

  onAdresseFixeInput(val: string): void {
    this.adresseSiegeFixe.set(val);
  }

  setMethodeRotation(m: 'tirage' | 'alphabetique' | 'accord'): void {
    this.methodeRotation.set(m);
  }

  onSubmitCreer(): void {
    if (!this.isFormValid()) {
      this.errorMessage.set('Veuillez vérifier les informations saisies.');
      return;
    }
    this.isSubmitting.set(true);
    // Redirection vers le dashboard
    this.router.navigate(['/tabs/dashboard']);
  }

  onSauvegarderBrouillon(): void {
    this.router.navigate(['/tabs/dashboard']);
  }

  onRetour(): void {
    this.router.navigate(['/tabs/dashboard']);
  }
}
