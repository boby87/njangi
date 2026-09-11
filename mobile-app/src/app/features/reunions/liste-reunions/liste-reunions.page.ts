import { Component } from '@angular/core';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular/standalone';

@Component({
  selector: 'app-liste-reunions',
  standalone: true,
  imports: [IonContent, IonHeader, IonTitle, IonToolbar],
  template: `
    <ion-header><ion-toolbar color="primary"><ion-title>Réunions</ion-title></ion-toolbar></ion-header>
    <ion-content class="ion-padding"><p>Liste des réunions à venir.</p></ion-content>
  `
})
export class ListeReunionsPage {}
