import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {environment} from '../../../environments/environments';
import {Observable, Subject, tap} from 'rxjs';
import {JwtTokenPairInfo} from '../interfaces/jwtTokenPairInfo.interface';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  http = inject(HttpClient);
  router = inject(Router);

  private refreshTimer: any;
  private onRefresh$ = new Subject<void>();

  onRefresh(): Observable<void> {
    return this.onRefresh$.asObservable();
  }

  refreshTokens() {
    return this.http.post<JwtTokenPairInfo>(`${environment.authApiEndpoint}/tokens/refresh`,
      {},
      {withCredentials: true})
      .pipe(
        tap((response) => {
          if (!response?.accessTokenExpiresAt) {
            console.error("Invalid refresh response", response);
            return;
          }

          this.scheduleRefresh(response.accessTokenExpiresAt);
          this.onRefresh$.next();
        })
      );
  }

  logout() {
    return this.http.post(`${environment.authApiEndpoint}/logout`, {}, {withCredentials: true})
      .subscribe(() => this.router.navigateByUrl('/auth/login'));
  }

  scheduleRefresh(expiresAt: number) {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }

    const delay = expiresAt - Date.now() - 60_000;

    this.refreshTimer = setTimeout(() => {
      this.refreshTokens().subscribe();
    }, Math.max(delay, 0));
  }
}
