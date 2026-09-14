import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TontineStateService } from '@core/services/tontine-state.service';
@Component({
  selector: 'app-createur-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './createur-view.component.html',
  styleUrl: './createur-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateurViewComponent {
  readonly tontineService = inject(TontineStateService);

  readonly groupe = this.tontineService.groupeActif;
  readonly membres = this.tontineService.membres;
  readonly premierBureauElu = computed(() => this.groupe().premierBureauElu);

  // Formulaire d'élection du premier bureau (Signal Forms purs)
  readonly presidentId = signal<string>('usr-1');
  readonly tresorierId = signal<string>('usr-2');
  readonly secretaireId = signal<string>('usr-3');

  copierLienInvitation(): void {
    const url = `https://njangi.cm/rejoindre/${this.groupe().id}`;
    if (navigator.clipboard) {
      navigator.clipboard.writeText(url);
    }
    alert(`Lien d'invitation copié : ${url}\nPartagez-le par WhatsApp ou SMS avec vos membres.`);
  }

  elirePremierBureau(): void {
    if (this.presidentId() === this.tresorierId() || this.presidentId() === this.secretaireId()) {
      alert('Le Président, le Trésorier et le Secrétaire doivent être des personnes distinctes.');
      return;
    }

    this.tontineService.elireBureau(
      this.groupe().id,
      this.presidentId(),
      this.tresorierId(),
      this.secretaireId()
    );

    alert('Félicitations ! Le premier bureau exécutif a été élu. En tant que créateur, vos pouvoirs techniques provisoires sont maintenant transférés au Président.');
  }
}
