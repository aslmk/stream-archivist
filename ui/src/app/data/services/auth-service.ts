import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  http = inject(HttpClient);

  refreshTokens() {
    return this.http.post('/api/auth/tokens/refresh', {}, {withCredentials: true});
  }
}
