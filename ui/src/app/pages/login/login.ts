import { Component } from '@angular/core';
import {environment} from '../../../environments/environments';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  standalone: true,
  styleUrls: ['./login.css']
})
export class LoginComponent {

  signInWithTwitch() {
    window.location.href = environment.twitchLoginUrl;
  }

}

