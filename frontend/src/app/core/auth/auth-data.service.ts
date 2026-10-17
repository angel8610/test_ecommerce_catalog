import { Injectable, signal } from '@angular/core';
import {AuthenticatedUserResponse} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthDataService {

  private readonly token = signal<string | null>(null);

  getAccessToken(): string | null {
    return this.token();
  }

  setData(accessToken: string, user: AuthenticatedUserResponse): void {
    this.setAccessToken(accessToken);
    this.setUserData(user);
  }

  setAccessToken(accessToken: string): void {
    this.token.set(accessToken);
  }

  getUserData(): null | AuthenticatedUserResponse {
    const data = sessionStorage.getItem('user_profile');
    if (!data) {
      return null;
    }

    try {
      return JSON.parse(atob(data));
    } catch {
      return null;
    }
  }

  clear(): void {
    this.token.set(null);
    sessionStorage.clear();
  }

  private setUserData(user: AuthenticatedUserResponse): void {
    try {
      const encryptedData = btoa(JSON.stringify(user));
      sessionStorage.setItem('user_profile', encryptedData);
    } catch (e) {
      console.error('Error to saved data user', e);
    }
  }


}
