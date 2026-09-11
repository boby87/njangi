import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { NavController } from '@ionic/angular/standalone';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonItem, IonLabel, IonInput, IonButton,
  IonText, IonSpinner
} from '@ionic/angular/standalone';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    IonContent, IonHeader, IonTitle, IonToolbar, IonCard, IonCardHeader,
    IonCardTitle, IonCardContent, IonItem, IonLabel, IonInput, IonButton,
    IonText, IonSpinner
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Njangi</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="login-wrapper">
        <ion-card>
          <ion-card-header>
            <ion-card-title>Connexion</ion-card-title>
          </ion-card-header>
          <ion-card-content>

            @if (errorMessage()) {
              <ion-text color="danger">
                <p>{{ errorMessage() }}</p>
              </ion-text>
            }

            <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
              <ion-item>
                <ion-label position="stacked">Email</ion-label>
                <ion-input
                  type="email"
                  formControlName="email"
                  placeholder="votre@email.com"
                ></ion-input>
              </ion-item>

              <ion-item>
                <ion-label position="stacked">Mot de passe</ion-label>
                <ion-input
                  type="password"
                  formControlName="motDePasse"
                  placeholder="Votre mot de passe"
                ></ion-input>
              </ion-item>

              <ion-button
                expand="block"
                type="submit"
                class="ion-margin-top"
                [disabled]="loginForm.invalid || isLoading()"
              >
                @if (isLoading()) {
                  <ion-spinner name="crescent"></ion-spinner>
                } @else {
                  Se connecter
                }
              </ion-button>
            </form>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  `,
  styles: [`
    .login-wrapper { display: flex; flex-direction: column; justify-content: center; min-height: 80vh; }
    ion-card { margin: 1rem; }
    ion-item { margin-bottom: 0.5rem; }
  `]
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly navCtrl = inject(NavController);

  readonly isLoading = signal(false);
  readonly errorMessage = signal('');

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    const { email, motDePasse } = this.loginForm.value;

    this.authService.login({ email: email!, motDePasse: motDePasse! }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.navCtrl.navigateRoot('/tabs/tableau-de-bord');
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.detail || 'Identifiants incorrects');
      }
    });
  }
}
