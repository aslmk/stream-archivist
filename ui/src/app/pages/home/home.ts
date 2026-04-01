import {Component, inject, OnInit} from '@angular/core';
import {Card} from '../../components/card/card';
import {StreamerStateService} from '../../data/services/streamerStateService';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {EmptyCard} from '../../components/empty-card/empty-card';
import {AuthService} from '../../data/services/authService';
import {SubscriptionService} from '../../data/services/subscriptionService';

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
  private streamerStateService = inject(StreamerStateService);
  private authService = inject(AuthService);
  private subscriptionService = inject(SubscriptionService);
  isUsernameFocused = false;
  isSubmitted = false;

  protected streamers = this.streamerStateService.streamersState;

  constructor() {
    this.streamerStateService.getTrackedStreamers();
  }

  ngOnInit(): void {
    this.authService.refreshTokens().subscribe();
  }

  protected form = new FormGroup({
    streamerUsername: new FormControl<string | null>(null, {
      nonNullable: false,
      validators: [Validators.required, Validators.minLength(1)]
    })
  });

  submitForm() {
    this.isSubmitted = true;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    let streamerUsername = this.form.value.streamerUsername;

    if (!streamerUsername || streamerUsername.trim() === '') {
      this.form.get('streamerUsername')?.markAsTouched();
      return;
    }

    streamerUsername = streamerUsername.trim();

    this.subscriptionService.subscribe(streamerUsername, 'twitch');
  }

  logout() {
    this.authService.logout();
  }
}
