import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { authGuard, guestGuard } from './auth.guard';

describe('authGuard & guestGuard', () => {
  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = { url: '/dashboard' } as RouterStateSnapshot;

  beforeEach(() => {
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  describe('on browser platform', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          provideRouter([]),
          provideHttpClient(),
          provideHttpClientTesting(),
          { provide: PLATFORM_ID, useValue: 'browser' },
        ],
      });
    });

    it('redirects unauthenticated users to /login', () => {
      const router = TestBed.inject(Router);
      const result = TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState));

      expect(result instanceof UrlTree).toBe(true);
      expect(router.serializeUrl(result as UrlTree)).toBe('/login');
    });

    it('allows authenticated users through authGuard', () => {
      const authService = TestBed.inject(AuthService);
      authService.session.update((s) => ({ ...s, token: 'valid-jwt-token' }));

      const result = TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState));

      expect(result).toBe(true);
    });

    it('redirects authenticated users away from /login via guestGuard', () => {
      const authService = TestBed.inject(AuthService);
      const router = TestBed.inject(Router);
      authService.session.update((s) => ({ ...s, token: 'valid-jwt-token' }));

      const result = TestBed.runInInjectionContext(() => guestGuard(dummyRoute, dummyState));

      expect(result instanceof UrlTree).toBe(true);
      expect(router.serializeUrl(result as UrlTree)).toBe('/dashboard');
    });

    it('allows unauthenticated users onto /login via guestGuard', () => {
      const result = TestBed.runInInjectionContext(() => guestGuard(dummyRoute, dummyState));

      expect(result).toBe(true);
    });
  });

  describe('on server platform (SSR prerendering)', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          provideRouter([]),
          provideHttpClient(),
          provideHttpClientTesting(),
          { provide: PLATFORM_ID, useValue: 'server' },
        ],
      });
    });

    it('allows SSR prerendering cleanly through authGuard', () => {
      const result = TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState));

      expect(result).toBe(true);
    });
  });
});
