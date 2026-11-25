import { Component, PLATFORM_ID, inject, signal, computed, effect, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
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
  private readonly platformId = inject(PLATFORM_ID);

  readonly groupe = this.tontineService.groupeActif;
  readonly membres = this.tontineService.membresGroupeActif;
  readonly premierBureauElu = computed(() => this.groupe().premierBureauElu);
  readonly peutElireBureau = computed(() => this.membres().length >= 3);

  // Formulaire d'élection du premier bureau (Signal Forms purs — zéro ID mocké)
  readonly presidentId = signal<string>('');
  readonly tresorierId = signal<string>('');
  readonly secretaireId = signal<string>('');
  readonly feedbackMessage = signal<string>('');
  readonly errorMessage = signal<string>('');

  constructor() {
    effect(() => {
      const list = this.membres();
      if (list.length >= 3) {
        if (!list.some((m) => m.id === this.presidentId())) {
          this.presidentId.set(list[0].id);
        }
        if (!list.some((m) => m.id === this.tresorierId())) {
          this.tresorierId.set(list[1].id);
        }
        if (!list.some((m) => m.id === this.secretaireId())) {
          this.secretaireId.set(list[2].id);
        }
      }
    });
  }

  copierCodeInvitation(): void {
    const code = this.groupe().codeInvitation || this.groupe().id;
    const texte = `Rejoignez notre tontine "${this.groupe().nom}" sur Njangi avec le code d'invitation : ${code}`;
    if (isPlatformBrowser(this.platformId) && navigator.clipboard) {
      navigator.clipboard.writeText(code);
    }
    this.errorMessage.set('');
    this.feedbackMessage.set(`Code copié (${code}) — ${texte}`);
  }

  elirePremierBureau(): void {
    this.errorMessage.set('');
    this.feedbackMessage.set('');

    if (!this.peutElireBureau()) {
      this.errorMessage.set('Il faut au moins 3 membres dans la tontine pour élire un bureau exécutif.');
      return;
    }

    const p = this.presidentId();
    const t = this.tresorierId();
    const s = this.secretaireId();

    if (!p || !t || !s || p === t || p === s || t === s) {
      this.errorMessage.set('Le Président, le Trésorier et le Secrétaire doivent être trois membres distincts.');
      return;
    }

    this.tontineService.elireBureau(this.groupe().id, p, t, s);
    this.feedbackMessage.set(
      'Premier bureau exécutif élu avec succès ! Les droits techniques provisoires de Créateur ont été transférés au Président.'
    );
  }
}
