import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Membre } from '@shared/models/membre.model';
import { RegisterPage } from './register.page';
import { MobileAuthService } from '../../core/services/auth.service';

describe('RegisterPage (Signal Forms pur — RxJS)', () => {
  let component: RegisterPage;
  let fixture: ComponentFixture<RegisterPage>;
  let router: Router;

  const pendingPhoneSignal = signal<string>('');
  const simulatedOtpSignal = signal<string | null>(null);
  const authErrorSignal = signal<string | null>(null);

  const mockMembre: Membre = {
    id: 'm-999',
    authUtilisateurId: 'u-999',
    nom: 'TCHOUA',
    prenom: 'Alain',
    email: '',
    telephone: '+237699123456',
    ville: 'Douala',
    pays: 'Cameroun',
    statut: 'ACTIF',
    dateInscription: '2026-03-01',
    estVerifieKyc: false,
  };

  const mockAuthService = {
    pendingPhone: pendingPhoneSignal,
    simulatedOtp: simulatedOtpSignal,
    authError: authErrorSignal,
    requestOtp: vi.fn((phone: string) => {
      pendingPhoneSignal.set(phone);
      return of(true);
    }),
    inscrire: vi.fn(() => of(mockMembre)),
    connexionSociale: vi.fn(() => of({ succes: true, telephoneRequis: false })),
  };

  beforeEach(() => {
    pendingPhoneSignal.set('');
    simulatedOtpSignal.set(null);
    authErrorSignal.set(null);
    mockAuthService.requestOtp.mockClear();
    mockAuthService.inscrire.mockClear();
    mockAuthService.connexionSociale.mockClear();

    TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [
        provideRouter([]),
        { provide: MobileAuthService, useValue: mockAuthService },
      ],
    });

    fixture = TestBed.createComponent(RegisterPage);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('devrait initialiser le formulaire sur l’étape FORM avec indicatif Cameroun +237', () => {
    expect(component).toBeTruthy();
    expect(component.etape()).toBe('FORM');
    expect(component.countryCode()).toBe('+237');
    expect(component.isFormValid()).toBe(false);
  });

  it('devrait valider le nom, le téléphone formaté et le code PIN 4 chiffres via Signals', () => {
    component.onFullNameInput('Alain TCHOUA');
    component.onPhoneInput('699123456');
    component.onPinDigitInput(0, '9');
    component.onPinDigitInput(1, '8');
    component.onPinDigitInput(2, '7');
    component.onPinDigitInput(3, '6');

    expect(component.isFullNameValid()).toBe(true);
    expect(component.cleanPhone()).toBe('699123456');
    expect(component.isPhoneValid()).toBe(true);
    expect(component.pin()).toBe('9876');
    expect(component.isPinValid()).toBe(true);
    expect(component.isFormValid()).toBe(true);
  });

  it('devrait appeler requestOtp via Observable et passer à l’étape OTP après soumission valide', () => {
    component.onFullNameInput('Alain TCHOUA');
    component.onPhoneInput('699123456');
    ['1', '2', '3', '4'].forEach((d, idx) => component.onPinDigitInput(idx, d));

    component.onSubmitRegister();

    expect(mockAuthService.requestOtp).toHaveBeenCalledWith('+237699123456');
    expect(component.etape()).toBe('OTP');
    expect(pendingPhoneSignal()).toBe('+237699123456');
  });

  it('devrait finaliser l’inscription via inscrire() et naviguer vers /tabs/dashboard', () => {
    const navSpy = vi
      .spyOn(router, 'navigate')
      .mockImplementation(() => true as unknown as ReturnType<Router['navigate']>);

    component.onFullNameInput('Alain TCHOUA');
    component.onPhoneInput('699123456');
    ['1', '2', '3', '4'].forEach((d, idx) => component.onPinDigitInput(idx, d));
    component.onSubmitRegister();

    component.onOtpInput('123456');
    expect(component.isOtpValid()).toBe(true);
    component.onVerifyOtp();

    expect(mockAuthService.inscrire).toHaveBeenCalledWith(
      expect.objectContaining({
        telephone: '+237699123456',
        prenom: 'Alain',
        nom: 'TCHOUA',
        ville: 'Douala',
        pays: 'Cameroun',
        codePin: '1234',
      })
    );
    expect(navSpy).toHaveBeenCalledWith(['/tabs/dashboard']);
  });

  it('devrait revenir de OTP à FORM lors du clic retour', () => {
    component.etape.set('OTP');
    component.onRetour();
    expect(component.etape()).toBe('FORM');
  });
});
