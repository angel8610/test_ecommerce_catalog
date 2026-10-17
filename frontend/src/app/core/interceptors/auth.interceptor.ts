import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import { inject } from '@angular/core';

import {AuthDataService} from '../auth/auth-data.service';
import {catchError, switchMap, throwError} from 'rxjs';
import {AuthService} from '../auth/auth.service';
import {AccessTokenResponse} from '../models/access-token-response.model';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authDataService = inject(AuthDataService);
  const authService = inject(AuthService);
  const token = authDataService.getAccessToken();

  if(!token || req.url.includes("/api/auth")) {
    return next(req);
  }

  const authRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authRequest).pipe(
    catchError((error) => {
      if(error instanceof HttpErrorResponse && error.status === 401
        && !authRequest.url.endsWith('auth/login')) {

        if(authRequest.url.endsWith('/auth/refresh')) {
          authService.logout();
          return throwError(() => error);
        }

        return authService.refreshToken().pipe(
          switchMap((response: AccessTokenResponse) => {
            const retryRequest = authRequest.clone({
              setHeaders: {
                Authorization: `Bearer ${response.accessToken}`
              }
            });
            return next(retryRequest);
          }),
          catchError((refreshError) => {
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );


};
