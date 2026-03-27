import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  http = inject(HttpClient);
  router = inject(Router);

  refreshTokens() {

    return this.http.post(`${environment.authApiEndpoint}/tokens/refresh`,
      {},
      {withCredentials: true});
  }

  logout() {
    return this.http.post(`${environment.authApiEndpoint}/logout`, {}, {withCredentials: true})
      .subscribe(() => this.router.navigateByUrl('/auth/login'));
  }
}
