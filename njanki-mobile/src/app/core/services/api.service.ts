import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, from, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { Capacitor } from '@capacitor/core';
import { Preferences } from '@capacitor/preferences';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';

const TOKEN_KEY = 'njangi_auth_token';

@Injectable({
  providedIn: 'root',
})
export class MobileApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = typeof window !== 'undefined' && !Capacitor.isNativePlatform()
    ? (environment.gatewayWebUrl || 'http://localhost:9090')
    : environment.apiUrl;

  /**
   * Effectue un appel GET via la Gateway avec injection transparente du token Bearer.
   * Zéro Promise / Zéro async/await : flux 100% RxJS.
   */
  get<T>(endpoint: string, params?: HttpParams): Observable<ApiResponse<T>> {
    return this.getAuthHeaders().pipe(
      switchMap((headers) =>
        this.http.get<ApiResponse<T>>(`${this.baseUrl}${endpoint}`, { headers, params })
      ),
      catchError((error) => this.handleError<T>(error))
    );
  }

  /**
   * Effectue un appel POST via la Gateway.
   */
  post<T>(endpoint: string, body: unknown): Observable<ApiResponse<T>> {
    return this.getAuthHeaders().pipe(
      switchMap((headers) =>
        this.http.post<ApiResponse<T>>(`${this.baseUrl}${endpoint}`, body, { headers })
      ),
      catchError((error) => this.handleError<T>(error))
    );
  }

  /**
   * Effectue un appel PUT via la Gateway.
   */
  put<T>(endpoint: string, body: unknown): Observable<ApiResponse<T>> {
    return this.getAuthHeaders().pipe(
      switchMap((headers) =>
        this.http.put<ApiResponse<T>>(`${this.baseUrl}${endpoint}`, body, { headers })
      ),
      catchError((error) => this.handleError<T>(error))
    );
  }

  /**
   * Effectue un appel DELETE via la Gateway.
   */
  delete<T>(endpoint: string): Observable<ApiResponse<T>> {
    return this.getAuthHeaders().pipe(
      switchMap((headers) =>
        this.http.delete<ApiResponse<T>>(`${this.baseUrl}${endpoint}`, { headers })
      ),
      catchError((error) => this.handleError<T>(error))
    );
  }

  private getAuthHeaders(): Observable<HttpHeaders> {
    return from(Preferences.get({ key: TOKEN_KEY })).pipe(
      switchMap(({ value }) => {
        let headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        });
        if (value) {
          headers = headers.set('Authorization', `Bearer ${value}`);
        }
        return of(headers);
      })
    );
  }

  private handleError<T>(error: { status?: number; message?: string; error?: { message?: string } }): Observable<ApiResponse<T>> {
    const errorMsg = error.error?.message || error.message || 'Erreur réseau ou passerelle inaccessible';
    return of({
      success: false,
      message: errorMsg,
      data: null as unknown as T,
      errorCode: `HTTP_${error.status || 500}`,
      timestamp: new Date().toISOString(),
    });
  }
}
