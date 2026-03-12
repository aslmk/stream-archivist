import {Component, inject} from '@angular/core';
import {Card} from '../../components/card/card';
import {StreamerStateService} from '../../data/services/streamer-state-service';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {EmptyCard} from '../../components/empty-card/empty-card';

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
export class Home {
  streamerStateService = inject(StreamerStateService);
  httpClient = inject(HttpClient);

  streamers = this.streamerStateService.streamers;

  constructor() {
    this.streamerStateService.loadInitial();
    this.streamerStateService.connectSse();
  }

  form = new FormGroup({
    streamerUsername: new FormControl(null, Validators.required)
  });

  submitForm() {
    const streamerUsername = this.form.value.streamerUsername;
    this.httpClient.post('/api/v1/subscriptions',
      {streamerUsername: streamerUsername, providerName: 'twitch'},
      {withCredentials: true})
      .subscribe(() => this.streamerStateService.loadInitial());
  }
}
