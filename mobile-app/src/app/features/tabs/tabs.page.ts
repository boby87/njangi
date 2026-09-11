import { Component } from '@angular/core';
import { IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { homeOutline, peopleOutline, calendarOutline, cashOutline } from 'ionicons/icons';

@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel],
  template: `
    <ion-tabs>
      <ion-tab-bar slot="bottom">
        <ion-tab-button tab="tableau-de-bord">
          <ion-icon name="home-outline"></ion-icon>
          <ion-label>Accueil</ion-label>
        </ion-tab-button>
        <ion-tab-button tab="groupes">
          <ion-icon name="people-outline"></ion-icon>
          <ion-label>Groupes</ion-label>
        </ion-tab-button>
        <ion-tab-button tab="reunions">
          <ion-icon name="calendar-outline"></ion-icon>
          <ion-label>Réunions</ion-label>
        </ion-tab-button>
        <ion-tab-button tab="cotisations">
          <ion-icon name="cash-outline"></ion-icon>
          <ion-label>Cotisations</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  `
})
export class TabsPage {
  constructor() {
    addIcons({ homeOutline, peopleOutline, calendarOutline, cashOutline });
  }
}
