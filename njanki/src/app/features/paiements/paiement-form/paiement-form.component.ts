import { Component, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TontineStateService } from '@core/services/tontine-state.service';
import { AuthService } from '@core/services/auth.service';
import { ModePaiement } from '@shared/models/paiement.model';
import { TypeCotisation } from '@shared/models/cotisation.model';
import { ProofPreviewComponent } from '@shared/ui/proof-preview/proof-preview.component';

@Component({
  selector: 'app-paiement-form',
  standalone: true,
  imports: [CommonModule, ProofPreviewComponent],
  templateUrl: './paiement-form.component.html',
  styleUrl: './paiement-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaiementFormComponent {
  private readonly tontineService = inject(TontineStateService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly groupe = this.tontineService.groupeActif;
  readonly session = this.tontineService.sessionActive;
  readonly membres = this.tontineService.membres;

  // État du formulaire en Signal Forms purs (zéro FormGroup/ngModel)
  readonly membreId = signal<string>(this.authService.utilisateur().id);
  readonly typeCotisation = signal<TypeCotisation>('ROTATIVE_POT');
  readonly montantXaf = signal<number>(100000);
  readonly modePaiement = signal<ModePaiement>('CASH');
  readonly referenceTransaction = signal<string>('');
  readonly preuveRecuBase64 = signal<string>('https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?fit=crop&w=600&h=400&q=80');
  readonly commentaire = signal<string>('Règlement séance ordinaire avec remise de reçu');
  readonly showProofModal = signal<boolean>(false);

  // Validation réactive avec computed()
  // RÈGLE OBLIGATOIRE DU PROJET : pour le CASH, la preuve (reçu signé) est obligatoire !
  readonly isValid = computed(() => {
    const membreOk = this.membreId().trim().length > 0;
    const montantOk = this.montantXaf() > 0;
    const preuveOk = this.modePaiement() !== 'CASH' || this.preuveRecuBase64().length > 0;
    return membreOk && montantOk && preuveOk;
  });

  onMembreChange(id: string): void { this.membreId.set(id); }
  onTypeCotisationChange(t: string): void { 
    const typ = t as TypeCotisation;
    this.typeCotisation.set(typ);
    if (typ === 'ROTATIVE_POT') this.montantXaf.set(100000);
    else if (typ === 'SECOURS_DECES') this.montantXaf.set(5000);
    else this.montantXaf.set(2000);
  }
  onMontantInput(val: string): void { this.montantXaf.set(parseInt(val, 10) || 0); }
  onModePaiementChange(m: string): void { 
    const mode = m as ModePaiement;
    this.modePaiement.set(mode);
    if (mode === 'MTN_MOMO') this.referenceTransaction.set('MTN-' + Date.now().toString().substring(6));
    else if (mode === 'ORANGE_MONEY') this.referenceTransaction.set('OM-' + Date.now().toString().substring(6));
  }
  onRefInput(val: string): void { this.referenceTransaction.set(val); }
  onCommentaireInput(val: string): void { this.commentaire.set(val); }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.preuveRecuBase64.set(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  voirPreuve(): void {
    this.showProofModal.set(true);
  }

  fermerPreuve(): void {
    this.showProofModal.set(false);
  }

  onSubmit(): void {
    if (!this.isValid()) return;

    this.tontineService.enregistrerPaiement({
      groupeId: this.groupe().id,
      sessionId: this.session()?.id || 'ses-1',
      membreId: this.membreId(),
      typeCotisation: this.typeCotisation(),
      montantXaf: this.montantXaf(),
      modePaiement: this.modePaiement(),
      referenceTransaction: this.referenceTransaction() || undefined,
      preuveRecuBase64: this.preuveRecuBase64(),
      cleIdempotence: 'idemp-' + Date.now(),
      commentaire: this.commentaire(),
    });

    alert('Paiement enregistré avec succès ! Quittance et reçu numérique enregistrés dans le registre immuable.');
    this.router.navigate(['/dashboard']);
  }

  onAnnuler(): void {
    this.router.navigate(['/dashboard']);
  }
}
