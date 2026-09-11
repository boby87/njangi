import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-tableau-de-bord',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard-container">
      <nav class="sidebar">
        <div class="logo">
          <h2>Njangi</h2>
        </div>
        <ul class="nav-links">
          <li><a routerLink="/tableau-de-bord">Tableau de bord</a></li>
          <li><a routerLink="/groupes">Mes groupes</a></li>
          <li><a routerLink="/membres">Membres</a></li>
          <li><a routerLink="/reunions">Réunions</a></li>
          <li><a routerLink="/cotisations">Cotisations</a></li>
        </ul>
        <div class="user-info">
          <p>{{ nomComplet() }}</p>
          <button (click)="logout()">Déconnexion</button>
        </div>
      </nav>

      <main class="main-content">
        <header>
          <h1>Bonjour, {{ prenomUtilisateur() }} !</h1>
          <p class="subtitle">Voici un résumé de votre activité Njangi</p>
        </header>

        <div class="stats-grid">
          <div class="stat-card">
            <h3>Groupes actifs</h3>
            <span class="stat-value">{{ stats().groupesActifs }}</span>
          </div>
          <div class="stat-card">
            <h3>Prochaine réunion</h3>
            <span class="stat-value">{{ stats().prochaineReunion }}</span>
          </div>
          <div class="stat-card">
            <h3>Cotisations en attente</h3>
            <span class="stat-value">{{ stats().cotisationsEnAttente }}</span>
          </div>
          <div class="stat-card">
            <h3>Pénalités</h3>
            <span class="stat-value stat-alert">{{ stats().penalites }}</span>
          </div>
        </div>

        <div class="quick-actions">
          <h2>Actions rapides</h2>
          <div class="actions-grid">
            <button class="action-btn" routerLink="/groupes">Voir mes groupes</button>
            <button class="action-btn" routerLink="/reunions">Planifier une réunion</button>
            <button class="action-btn" routerLink="/cotisations">Gérer les cotisations</button>
          </div>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-container { display: flex; min-height: 100vh; }
    .sidebar { width: 250px; background: #1a237e; color: white; display: flex; flex-direction: column; padding: 1rem; }
    .logo h2 { color: #ffd54f; margin-bottom: 2rem; }
    .nav-links { list-style: none; padding: 0; flex: 1; }
    .nav-links li { margin-bottom: 0.5rem; }
    .nav-links a { color: #c5cae9; text-decoration: none; padding: 0.5rem 1rem; border-radius: 6px; display: block; }
    .nav-links a:hover { background: rgba(255,255,255,0.1); color: white; }
    .user-info { border-top: 1px solid rgba(255,255,255,0.2); padding-top: 1rem; }
    .user-info p { font-size: 0.9rem; margin-bottom: 0.5rem; }
    .user-info button { background: transparent; border: 1px solid rgba(255,255,255,0.3); color: white; padding: 0.4rem 0.8rem; border-radius: 4px; cursor: pointer; width: 100%; }
    .main-content { flex: 1; padding: 2rem; background: #f8f9fa; }
    header { margin-bottom: 2rem; }
    h1 { color: #333; }
    .subtitle { color: #777; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
    .stat-card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .stat-card h3 { font-size: 0.85rem; color: #777; margin-bottom: 0.5rem; }
    .stat-value { font-size: 2rem; font-weight: bold; color: #1a237e; }
    .stat-alert { color: #c62828; }
    .quick-actions h2 { margin-bottom: 1rem; color: #333; }
    .actions-grid { display: flex; gap: 1rem; flex-wrap: wrap; }
    .action-btn { background: #1a237e; color: white; border: none; padding: 0.75rem 1.5rem; border-radius: 6px; cursor: pointer; font-size: 0.95rem; }
    .action-btn:hover { background: #283593; }
  `]
})
export class TableauDeBordComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly stats = signal({ groupesActifs: 0, prochaineReunion: 'Aucune', cotisationsEnAttente: 0, penalites: 0 });

  readonly nomComplet = computed(() => {
    const user = this.authService.currentUser();
    return user ? `${user.prenom} ${user.nom}` : '';
  });

  readonly prenomUtilisateur = computed(() => this.authService.currentUser()?.prenom ?? '');

  ngOnInit(): void {
    // Charger les stats depuis l'API
    this.stats.set({ groupesActifs: 3, prochaineReunion: 'Samedi 15 Sep', cotisationsEnAttente: 1, penalites: 0 });
  }

  logout(): void {
    this.authService.logout();
  }
}
