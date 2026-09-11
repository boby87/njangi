import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem,
  IonLabel, IonBadge, IonRefresher, IonRefresherContent,
  IonFab, IonFabButton, IonIcon, IonSkeletonText
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { addOutline } from 'ionicons/icons';
import { environment } from '../../../../environments/environment';

interface Groupe {
  id: string;
  nom: string;
  description: string;
  typeSiege: string;
  statut: string;
  montantCotisationPrincipale: number;
}

@Component({
  selector: 'app-liste-groupes',
  standalone: true,
  imports: [
    CommonModule, IonContent, IonHeader, IonTitle, IonToolbar, IonList,
    IonItem, IonLabel, IonBadge, IonRefresher, IonRefresherContent,
    IonFab, IonFabButton, IonIcon, IonSkeletonText
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Mes Groupes</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <ion-refresher slot="fixed" (ionRefresh)="refresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      @if (isLoading()) {
        <ion-list>
          @for (i of [1,2,3]; track i) {
            <ion-item>
              <ion-label>
                <ion-skeleton-text animated style="width: 70%"></ion-skeleton-text>
                <ion-skeleton-text animated style="width: 50%"></ion-skeleton-text>
              </ion-label>
            </ion-item>
          }
        </ion-list>
      } @else {
        <ion-list>
          @for (groupe of groupes(); track groupe.id) {
            <ion-item>
              <ion-label>
                <h2>{{ groupe.nom }}</h2>
                <p>{{ groupe.montantCotisationPrincipale | number:'1.0-0' }} FCFA · {{ groupe.typeSiege }}</p>
              </ion-label>
              <ion-badge slot="end" [color]="groupe.statut === 'ACTIF' ? 'success' : 'medium'">
                {{ groupe.statut }}
              </ion-badge>
            </ion-item>
          }
        </ion-list>
      }

      <ion-fab vertical="bottom" horizontal="end" slot="fixed">
        <ion-fab-button>
          <ion-icon name="add-outline"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>
  `
})
export class ListeGroupesPage implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/groupes`;

  readonly groupes = signal<Groupe[]>([]);
  readonly isLoading = signal(true);

  constructor() {
    addIcons({ addOutline });
  }

  ngOnInit(): void {
    this.chargerGroupes();
  }

  chargerGroupes(): void {
    this.isLoading.set(true);
    this.http.get<Groupe[]>(this.apiUrl).subscribe({
      next: (data) => { this.groupes.set(data); this.isLoading.set(false); },
      error: () => { this.isLoading.set(false); }
    });
  }

  refresh(event: any): void {
    this.chargerGroupes();
    event.target.complete();
  }
}
