import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import {computed, inject, Injectable} from '@angular/core';
import {catchError, finalize, Observable, of, shareReplay, tap} from 'rxjs';

import {
  LoginRequest,
  LoginResponse
} from '../models/auth.models';
import { environment } from '../../../environments/environment';
import {AuthDataService} from './auth-data.service';
import {AccessTokenResponse} from '../models/access-token-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly authDataService = inject(AuthDataService);

  private readonly apiUrl = environment.apiBaseAuthUrl
  readonly isAuthenticated = computed(
    ()=> this.authDataService.getAccessToken() !== null);
  private refreshTokenInProgress: Observable<any> | null = null;

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${ this.apiUrl }/login`, credentials, { withCredentials: true })
      .pipe(
        tap(response => {
          this.authDataService.setData(response.accessToken, response.user);
        })
      );
  }

  refreshToken(): Observable<any> {
    if(this.refreshTokenInProgress) {
      return this.refreshTokenInProgress;
    }

    this.refreshTokenInProgress = this.http.post<AccessTokenResponse>(
      `${this.apiUrl}/refresh`, {}, { withCredentials: true }).pipe(
      tap(response => {
        this.authDataService.setAccessToken(response.accessToken)
        this.refreshTokenInProgress = null;
      }),
      shareReplay(1)
    );

    return this.refreshTokenInProgress;
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${ this.apiUrl }/logout`, {}, { withCredentials: true }).pipe(
      catchError(() => of(void 0)),
      finalize(() => {
        this.router.navigateByUrl('/login');
        this.authDataService.clear();
      })
    );
  }


}
