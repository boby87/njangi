import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs/operators';
import { MobileAuthService } from '../services/auth.service';

/**
 * Garde d'authentification mobile (100% réactif Signals + RxJS).
 * Attend la restauration de la session depuis @capacitor/preferences avant de décider.
 */
export const mobileAuthGuard: CanActivateFn = () => {
  const authService = inject(MobileAuthService);
  const router = inject(Router);

  return toObservable(authService.sessionReady).pipe(
    filter(Boolean),
    take(1),
    map(() => (authService.isAuthenticated() ? true : router.createUrlTree(['/login'])))
  );
};

/**
 * Garde invité mobile : redirige les utilisateurs déjà authentifiés
 * loin de /login et /register vers /tabs/dashboard.
 */
export const mobileGuestGuard: CanActivateFn = () => {
  const authService = inject(MobileAuthService);
  const router = inject(Router);

  return toObservable(authService.sessionReady).pipe(
    filter(Boolean),
    take(1),
    map(() => (authService.isAuthenticated() ? router.createUrlTree(['/tabs/dashboard']) : true))
  );
};
