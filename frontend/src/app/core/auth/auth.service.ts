import {inject, Injectable, signal} from '@angular/core';
import {Observable, tap} from 'rxjs';

import {
  AuthUser,
  LoginRequest,
  LoginResponse
} from '../models/auth.models';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseAuthUrl
  private readonly token = signal<string | null>(null);
  private readonly currentUser = signal<AuthUser | null>(null);
  readonly isAuthenticated = () => this.token() !== null;
  readonly user = this.currentUser.asReadonly();

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.token.set(response.accessToken);
          //this.currentUser.set(response.user);
        })
      );
  }

  getAccessToken(): string | null {
    return this.token();
  }

  logout(): void {
    this.token.set(null);
    this.currentUser.set(null);
  }


}
