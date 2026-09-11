import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonGrid, IonRow, IonCol, IonButton
} from '@ionic/angular/standalone';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-tableau-de-bord',
  standalone: true,
  imports: [
    CommonModule, IonContent, IonHeader, IonTitle, IonToolbar, IonCard,
    IonCardHeader, IonCardTitle, IonCardContent, IonGrid, IonRow, IonCol, IonButton
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Njangi - Accueil</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <h2>Bonjour, {{ prenomUtilisateur() }} !</h2>
      <p>Bienvenue dans votre espace Njangi.</p>

      <ion-grid>
        <ion-row>
          <ion-col size="6">
            <ion-card>
              <ion-card-content class="stat-card">
                <div class="stat-value">3</div>
                <div class="stat-label">Groupes actifs</div>
              </ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card>
              <ion-card-content class="stat-card">
                <div class="stat-value">1</div>
                <div class="stat-label">Cotisations dues</div>
              </ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
        <ion-row>
          <ion-col size="6">
            <ion-card>
              <ion-card-content class="stat-card">
                <div class="stat-value">Sam 15</div>
                <div class="stat-label">Prochaine réunion</div>
              </ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card>
              <ion-card-content class="stat-card">
                <div class="stat-value text-success">0</div>
                <div class="stat-label">Pénalités</div>
              </ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
      </ion-grid>

      <ion-button expand="block" color="danger" (click)="logout()" class="ion-margin-top">
        Se déconnecter
      </ion-button>
    </ion-content>
  `,
  styles: [`
    .stat-card { text-align: center; }
    .stat-value { font-size: 1.8rem; font-weight: bold; color: #1a237e; }
    .stat-value.text-success { color: #2e7d32; }
    .stat-label { font-size: 0.8rem; color: #777; margin-top: 0.25rem; }
  `]
})
export class TableauDeBordPage {
  private readonly authService = inject(AuthService);

  readonly prenomUtilisateur = computed(() => this.authService.currentUser()?.prenom ?? 'Membre');

  logout(): void {
    this.authService.logout();
  }
}
