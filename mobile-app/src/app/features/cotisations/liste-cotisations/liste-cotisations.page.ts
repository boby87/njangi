import { Component } from '@angular/core';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular/standalone';

@Component({
  selector: 'app-liste-cotisations',
  standalone: true,
  imports: [IonContent, IonHeader, IonTitle, IonToolbar],
  template: `
    <ion-header><ion-toolbar color="primary"><ion-title>Cotisations</ion-title></ion-toolbar></ion-header>
    <ion-content class="ion-padding"><p>Suivi de vos cotisations.</p></ion-content>
  `
})
export class ListeCotisationsPage {}
