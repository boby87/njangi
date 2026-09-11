import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../../environments/environment';

export interface Groupe {
  id: string;
  nom: string;
  description: string;
  typeSiege: string;
  statut: string;
  montantCotisationPrincipale: number;
  frequenceReunion: string;
  createdAt: string;
}

@Component({
  selector: 'app-liste-groupes',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <h1>Mes Groupes Njangi</h1>
        <button class="btn-primary" (click)="creerGroupe()">+ Nouveau groupe</button>
      </header>

      @if (isLoading()) {
        <div class="loading">Chargement des groupes...</div>
      } @else if (groupes().length === 0) {
        <div class="empty-state">
          <p>Vous n'êtes membre d'aucun groupe pour le moment.</p>
          <button class="btn-primary" (click)="creerGroupe()">Créer votre premier groupe</button>
        </div>
      } @else {
        <div class="groupes-grid">
          @for (groupe of groupes(); track groupe.id) {
            <div class="groupe-card" (click)="voirGroupe(groupe.id)">
              <div class="groupe-header">
                <h2>{{ groupe.nom }}</h2>
                <span class="statut-badge" [class]="'statut-' + groupe.statut.toLowerCase()">
                  {{ groupe.statut }}
                </span>
              </div>
              <p class="description">{{ groupe.description || 'Aucune description' }}</p>
              <div class="groupe-details">
                <span>Siège: {{ groupe.typeSiege }}</span>
                <span>Fréquence: {{ groupe.frequenceReunion }}</span>
                <span>Cotisation: {{ groupe.montantCotisationPrincipale | number:'1.0-0' }} FCFA</span>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container { padding: 2rem; max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    h1 { color: #333; }
    .btn-primary { background: #007bff; color: white; border: none; padding: 0.6rem 1.2rem; border-radius: 6px; cursor: pointer; font-size: 0.95rem; }
    .loading, .empty-state { text-align: center; padding: 3rem; color: #777; }
    .groupes-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; }
    .groupe-card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.08); cursor: pointer; transition: transform 0.2s; }
    .groupe-card:hover { transform: translateY(-2px); }
    .groupe-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem; }
    h2 { font-size: 1.1rem; color: #333; margin: 0; }
    .statut-badge { padding: 0.2rem 0.6rem; border-radius: 20px; font-size: 0.8rem; }
    .statut-actif { background: #e8f5e9; color: #2e7d32; }
    .statut-inactif { background: #f5f5f5; color: #757575; }
    .description { color: #666; font-size: 0.9rem; margin: 0.5rem 0 1rem; }
    .groupe-details { display: flex; flex-direction: column; gap: 0.3rem; font-size: 0.85rem; color: #555; }
  `]
})
export class ListeGroupesComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/groupes`;

  readonly groupes = signal<Groupe[]>([]);
  readonly isLoading = signal(true);

  ngOnInit(): void {
    this.chargerGroupes();
  }

  chargerGroupes(): void {
    this.isLoading.set(true);
    this.http.get<Groupe[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.groupes.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  creerGroupe(): void {
    this.router.navigate(['/groupes/nouveau']);
  }

  voirGroupe(id: string): void {
    this.router.navigate(['/groupes', id]);
  }
}
