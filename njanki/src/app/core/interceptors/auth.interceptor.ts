import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { environment } from '../../../environments/environment';

const PUBLIC_AUTH_ENDPOINTS = [
  '/auth/login',
  '/auth/connexion',
  '/auth/inscrire',
  '/auth/otp/',
  '/auth/social/login',
  '/auth/social/connexion',
];

function isApiRequest(url: string): boolean {
  return url.startsWith(environment.apiUrl);
}

function isPublicAuthEndpoint(url: string): boolean {
  return PUBLIC_AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint));
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const apiReq = isApiRequest(req.url);
  const publicAuth = isPublicAuthEndpoint(req.url);

  if (!apiReq || publicAuth) {
    return next(req);
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.session().token;

  const authenticatedReq =
    token && !req.headers.has('Authorization')
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        })
      : req;

  return next(authenticatedReq).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        authService.deconnecter();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
