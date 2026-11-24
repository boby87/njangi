import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('starts unauthenticated when sessionStorage is empty', () => {
    expect(service.estConnecte()).toBe(false);
    expect(service.session().token).toBe('');
    expect(service.utilisateur().id).toBe('');
  });

  it('calls POST /auth/otp/demander and updates OTP state', () => {
    let completed = false;
    service.demanderOtp('+237699123456').subscribe((res) => {
      expect(res.success).toBe(true);
      completed = true;
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/otp/demander`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ identifiant: '+237699123456' });

    req.flush({
      success: true,
      message: 'Code OTP généré avec succès',
      data: null,
    });

    expect(completed).toBe(true);
    expect(service.otpEnvoye()).toBe(true);
    expect(service.dernierIdentifiant()).toBe('+237699123456');
  });

  it('verifies OTP, stores JWT + user in sessionStorage, and loads adhesions', () => {
    let loginResult = false;
    service.verifierOtp('+237699123456', '123456').subscribe((ok) => {
      loginResult = ok;
    });

    const otpReq = httpMock.expectOne(`${environment.apiUrl}/auth/otp/verifier`);
    expect(otpReq.request.method).toBe('POST');
    expect(otpReq.request.body).toEqual({ identifiant: '+237699123456', code: '123456' });

    otpReq.flush({
      success: true,
      message: 'Authentification par OTP réussie',
      data: {
        accessToken: 'jwt-real-token-123',
        tokenType: 'Bearer',
        expiresIn: 86400,
        utilisateur: {
          id: '550e8400-e29b-41d4-a716-446655440000',
          telephone: '+237699123456',
          email: 'jp.mbarga@njangi.cm',
          nom: 'Mbarga',
          prenom: 'Jean-Paul',
          ville: 'Douala',
          pays: 'Cameroun',
          statut: 'ACTIF',
          creeLe: '2026-01-15T10:00:00',
        },
      },
    });

    const adhReq = httpMock.expectOne(
      `${environment.apiUrl}/membres/utilisateurs/550e8400-e29b-41d4-a716-446655440000/adhesions`
    );
    expect(adhReq.request.method).toBe('GET');

    adhReq.flush([
      {
        id: 'adh-1',
        utilisateurId: '550e8400-e29b-41d4-a716-446655440000',
        groupeId: 'grp-100',
        role: 'PRESIDENT',
        statut: 'ACTIF',
        rejointLe: '2026-02-01T08:00:00',
      },
    ]);

    expect(loginResult).toBe(true);
    expect(service.estConnecte()).toBe(true);
    expect(service.session().token).toBe('jwt-real-token-123');
    expect(service.utilisateur().nom).toBe('Mbarga');
    expect(service.adhesions().length).toBe(1);
    expect(service.groupeActifId()).toBe('grp-100');

    const stored = sessionStorage.getItem('njangi_web_session');
    expect(stored).toBeTruthy();
    expect(stored).toContain('jwt-real-token-123');
  });

  it('falls back to empty adhesions when adhesions endpoint fails', () => {
    let loginResult = false;
    service.verifierOtp('+237699123456', '654321').subscribe((ok) => {
      loginResult = ok;
    });

    const otpReq = httpMock.expectOne(`${environment.apiUrl}/auth/otp/verifier`);
    otpReq.flush({
      success: true,
      message: 'OK',
      data: {
        accessToken: 'jwt-fallback-token',
        utilisateur: {
          id: 'usr-fallback-1',
          telephone: '+237699123456',
          nom: 'Kamga',
          prenom: 'Pierre',
        },
      },
    });

    const adhReq = httpMock.expectOne(
      `${environment.apiUrl}/membres/utilisateurs/usr-fallback-1/adhesions`
    );
    adhReq.flush('Not Found', { status: 404, statusText: 'Not Found' });

    expect(loginResult).toBe(true);
    expect(service.estConnecte()).toBe(true);
    expect(service.adhesions()).toEqual([]);
  });

  it('clears session, removes sessionStorage, and navigates to /login on deconnecter', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    service.session.set({
      membre: {
        id: 'usr-1',
        nom: 'Mbarga',
        prenom: 'Jean-Paul',
        telephone: '+237699123456',
        email: 'jp@njangi.cm',
        ville: 'Douala',
        pays: 'Cameroun',
        dateInscription: '2026-01-01',
        estVerifieKyc: true,
      },
      token: 'jwt-to-clear',
      adhesions: [],
      groupeActifId: 'grp-1',
    });
    sessionStorage.setItem('njangi_web_session', JSON.stringify(service.session()));

    service.deconnecter();

    expect(service.estConnecte()).toBe(false);
    expect(service.session().token).toBe('');
    expect(sessionStorage.getItem('njangi_web_session')).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
