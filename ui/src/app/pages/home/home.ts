import {Component, inject, OnInit} from '@angular/core';
import {Card} from '../../components/card/card';
import {StreamerStateService} from '../../data/services/streamer-state-service';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {EmptyCard} from '../../components/empty-card/empty-card';
import {AuthService} from '../../data/services/auth-service';
import {environment} from '../../../environments/environments';

@Component({
  selector: 'app-home',
  imports: [
    Card,
    EmptyCard,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
  standalone: true
})
export class Home implements OnInit {
  streamerStateService = inject(StreamerStateService);
  httpClient = inject(HttpClient);
  authService = inject(AuthService);

  streamers = this.streamerStateService.streamersState;

  constructor() {
    this.streamerStateService.loadInitial();
  }

  ngOnInit(): void {
    this.authService.refreshTokens().subscribe();
  }

  form = new FormGroup({
    streamerUsername: new FormControl(null, Validators.required)
  });

  submitForm() {
    const streamerUsername = this.form.value.streamerUsername;
    this.httpClient.post(`${environment.subscriptionsApiEndpoint}`,
      {streamerUsername: streamerUsername, providerName: 'twitch'},
      {withCredentials: true})
      .subscribe(() => this.streamerStateService.loadInitial());
  }

  logout() {
    this.authService.logout();
  }
}
