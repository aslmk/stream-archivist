import {inject, Injectable} from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import {catchError, EMPTY, Observable, switchMap, throwError} from 'rxjs';
import {Router} from '@angular/router';
import {AuthService} from '../data/services/authService';
import {environment} from '../../environments/environments';


@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  router = inject(Router);
  authService = inject(AuthService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    return next.handle(req).pipe(catchError((err: HttpErrorResponse) => {

      if (err.status === 401) {
        if (req.url.includes(`${environment.authApiEndpoint}/tokens/refresh`)) {
          this.router.navigateByUrl('/auth/login');
          return EMPTY;
        } else {
          return this.authService.refreshTokens().pipe(
            switchMap(() => next.handle(req)),
            catchError(() => {
              this.router.navigateByUrl('/auth/login');
              return EMPTY;
            })
          );
        }
      }

      return throwError(() => err);
    }));
  }
}
