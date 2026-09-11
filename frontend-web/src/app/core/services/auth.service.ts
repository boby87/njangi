import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Utilisateur {
  id: string;
  email: string;
  telephone: string;
  nom: string;
  prenom: string;
  statut: string;
}

export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  utilisateur: Utilisateur;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/auth`;

  private readonly _currentUser = signal<Utilisateur | null>(this.loadUserFromStorage());
  private readonly _token = signal<string | null>(localStorage.getItem('njangi_token'));

  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null && this._token() !== null);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem('njangi_token', response.accessToken);
        localStorage.setItem('njangi_user', JSON.stringify(response.utilisateur));
        this._token.set(response.accessToken);
        this._currentUser.set(response.utilisateur);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('njangi_token');
    localStorage.removeItem('njangi_user');
    this._token.set(null);
    this._currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  genererOtp(telephone: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/otp/generer?telephone=${telephone}`, {});
  }

  verifierOtp(telephone: string, otpCode: string): Observable<boolean> {
    return this.http.post<boolean>(`${this.apiUrl}/otp/verifier`, { telephone, otpCode });
  }

  private loadUserFromStorage(): Utilisateur | null {
    const userJson = localStorage.getItem('njangi_user');
    return userJson ? JSON.parse(userJson) : null;
  }
}
