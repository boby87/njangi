import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { RegisterPage } from './register.page';
import { MobileAuthService } from '../../core/services/auth.service';

describe('RegisterPage', () => {
  let component: RegisterPage;
  let authService: MobileAuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
      ],
    });

    const fixture = TestBed.createComponent(RegisterPage);
    component = fixture.componentInstance;
    authService = TestBed.inject(MobileAuthService);
  });

  it('devrait être instancié avec succès', () => {
    expect(component).toBeTruthy();
    expect(component.etape()).toBe('PHONE');
    expect(component.indicatifChoisi()).toBe('+237');
  });

  it('devrait valider réactivement le numéro de téléphone', () => {
    // Invalide avec 4 chiffres
    component.onNumeroLocalInput('6991');
    expect(component.isPhoneValid()).toBe(false);

    // Valide avec 9 chiffres pour le Cameroun (+237)
    component.onNumeroLocalInput('699123456');
    expect(component.isPhoneValid()).toBe(true);
    expect(component.telephoneComplet()).toBe('+237699123456');
  });

  it('devrait passer à l’étape OTP après demande de code', () => {
    component.onNumeroLocalInput('699123456');
    component.onRequestOtp();

    expect(component.etape()).toBe('OTP');
    expect(component.isOtpValid()).toBe(true); // Pré-rempli avec code simulé
    expect(component.countdown()).toBe(60);
  });

  it('devrait valider le code OTP et passer à l’étape PROFIL', () => {
    component.onNumeroLocalInput('699123456');
    component.onRequestOtp();

    // Saisie du code OTP valide généré ou code passe-partout 123456
    const codeValide = authService.simulatedOtp() || '123456';
    component.otpDigits.set(codeValide.split(''));
    component.onVerifyOtp();

    expect(component.etape()).toBe('PROFIL');
  });

  it('devrait contrôler la validité des champs du profil', () => {
    component.etape.set('PROFIL');

    // Invalide si nom ou prénom manquant
    component.onNomInput('');
    component.onPrenomInput('');
    expect(component.isProfileValid()).toBe(false);

    component.onNomInput('KAMGA');
    component.onPrenomInput('Eric');
    component.onVilleInput('Douala');
    component.onEmailInput('eric.kamga@njangi.cm');

    expect(component.isProfileValid()).toBe(true);
  });

  it('devrait calculer la force du mot de passe et valider la sécurité', () => {
    component.etape.set('SECURITE');

    // Mot de passe faible
    component.onMotDePasseInput('12345');
    expect(component.passwordStrength()).toBe('FAIBLE');

    // Mot de passe moyen
    component.onMotDePasseInput('Njangi123');
    expect(component.passwordStrength()).toBe('MOYEN');

    // Mot de passe fort
    component.onMotDePasseInput('Njangi2026!#');
    expect(component.passwordStrength()).toBe('FORT');

    component.onConfirmerMotDePasseInput('Njangi2026!#');
    component.onCodePinInput('4321');
    component.onConfirmerCodePinInput('4321');
    component.charteAcceptee.set(true);

    expect(component.isSecurityValid()).toBe(true);
  });

  it('devrait finaliser l’inscription et afficher l’écran de succès avec carte membre', () => {
    component.onNumeroLocalInput('699123456');
    component.onNomInput('TCHOUA');
    component.onPrenomInput('Alain');
    component.onVilleInput('Yaoundé');
    component.onMotDePasseInput('SecretPass123!');
    component.onCodePinInput('9876');

    component.onTerminerInscription(false);

    expect(component.etape()).toBe('SUCCES');
    expect(component.membreInscrit()).toBeTruthy();
    expect(component.membreInscrit()?.nom).toBe('TCHOUA');
    expect(component.membreInscrit()?.prenom).toBe('Alain');
    expect(component.membreInscrit()?.telephone).toBe('+237699123456');
    expect(authService.currentUser()?.nom).toBe('TCHOUA');
  });

  it('devrait permettre de naviguer en arrière entre les étapes', () => {
    component.etape.set('OTP');
    component.onRetourArriere();
    expect(component.etape()).toBe('PHONE');

    component.etape.set('PROFIL');
    component.onRetourArriere();
    expect(component.etape()).toBe('OTP');

    component.etape.set('SECURITE');
    component.onRetourArriere();
    expect(component.etape()).toBe('PROFIL');

    component.etape.set('KYC');
    component.onRetourArriere();
    expect(component.etape()).toBe('SECURITE');
  });
});
