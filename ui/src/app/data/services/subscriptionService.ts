import {inject, Injectable} from '@angular/core';
import {StreamerService} from './streamerService';
import {StreamerStateService} from './streamer-state-service';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  streamerService = inject(StreamerService);
  streamerStateService = inject(StreamerStateService);

  unsubscribe(streamerId: string) {
    this.streamerService.unsubscribeFromStreamer(streamerId)
      .subscribe(() => this.streamerStateService.loadInitial());
  }
}
