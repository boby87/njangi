import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { signal } from '@angular/core';
import { Observable } from 'rxjs';
import { mobileAuthGuard, mobileGuestGuard } from './auth.guard';
import { MobileAuthService } from '../services/auth.service';

describe('Mobile Auth Guards (100% Signals + RxJS)', () => {
  const sessionReadySignal = signal<boolean>(false);
  const isAuthenticatedSignal = signal<boolean>(false);

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/tabs/dashboard' } as RouterStateSnapshot;

  let router: Router;

  beforeEach(() => {
    sessionReadySignal.set(false);
    isAuthenticatedSignal.set(false);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: MobileAuthService,
          useValue: {
            sessionReady: sessionReadySignal,
            isAuthenticated: isAuthenticatedSignal,
          },
        },
      ],
    });

    router = TestBed.inject(Router);
  });

  it('mobileAuthGuard devrait attendre sessionReady puis rediriger vers /login si non authentifié', () => {
    let result: boolean | UrlTree | undefined;

    const guard$ = TestBed.runInInjectionContext(() =>
      mobileAuthGuard(mockRoute, mockState)
    ) as Observable<boolean | UrlTree>;

    guard$.subscribe((val) => {
      result = val;
    });

    TestBed.tick();
    expect(result).toBeUndefined();

    sessionReadySignal.set(true);
    TestBed.tick();

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });

  it('mobileAuthGuard devrait autoriser l’accès si sessionReady et isAuthenticated sont vrais', () => {
    sessionReadySignal.set(true);
    isAuthenticatedSignal.set(true);

    let result: boolean | UrlTree | undefined;

    const guard$ = TestBed.runInInjectionContext(() =>
      mobileAuthGuard(mockRoute, mockState)
    ) as Observable<boolean | UrlTree>;

    guard$.subscribe((val) => {
      result = val;
    });

    TestBed.tick();

    expect(result).toBe(true);
  });

  it('mobileGuestGuard devrait autoriser /login si l’utilisateur n’est pas authentifié', () => {
    sessionReadySignal.set(true);
    isAuthenticatedSignal.set(false);

    let result: boolean | UrlTree | undefined;

    const guard$ = TestBed.runInInjectionContext(() =>
      mobileGuestGuard(mockRoute, { url: '/login' } as RouterStateSnapshot)
    ) as Observable<boolean | UrlTree>;

    guard$.subscribe((val) => {
      result = val;
    });

    TestBed.tick();

    expect(result).toBe(true);
  });

  it('mobileGuestGuard devrait rediriger vers /tabs/dashboard si l’utilisateur est déjà authentifié', () => {
    sessionReadySignal.set(true);
    isAuthenticatedSignal.set(true);

    let result: boolean | UrlTree | undefined;

    const guard$ = TestBed.runInInjectionContext(() =>
      mobileGuestGuard(mockRoute, { url: '/login' } as RouterStateSnapshot)
    ) as Observable<boolean | UrlTree>;

    guard$.subscribe((val) => {
      result = val;
    });

    TestBed.tick();

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree)).toBe('/tabs/dashboard');
  });
});
