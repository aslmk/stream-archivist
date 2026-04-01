import {inject, Injectable} from '@angular/core';
import {StreamerService} from './streamerService';
import {StreamerStateService} from './streamerStateService';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  private streamerService = inject(StreamerService);
  private streamerStateService = inject(StreamerStateService);

  subscribe(streamerUsername: string, providerName: string) {
    this.streamerService.subscribeToStreamer(streamerUsername, providerName)
      .subscribe(() => this.streamerStateService.getTrackedStreamers());
  }

  unsubscribe(streamerId: string) {
    this.streamerService.unsubscribeFromStreamer(streamerId)
      .subscribe(() => this.streamerStateService.getTrackedStreamers());
  }
}
