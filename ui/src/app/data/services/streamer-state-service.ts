import {inject, Injectable, signal} from '@angular/core';
import {StreamerView} from '../interfaces/StreamerView.interface';
import {StreamerService} from './streamerService';
import {StreamerStateEvent} from '../events/StreamerStateEvent.interface';
import {environment} from '../../../environments/environments';
import {AuthService} from './auth-service';

@Injectable({
  providedIn: 'root',
})
export class StreamerStateService {
  streamerService = inject(StreamerService)
  authService = inject(AuthService);

  streamers = signal<StreamerView[]>([])

  private eventSource?: EventSource;

  constructor() {
    this.authService.onRefresh().subscribe(() => {
      this.reconnectSse();
    });
  }

  loadInitial() {
    this.streamerService.getTrackedStreamers()
      .subscribe(val =>
        this.streamers.set(val));
  }

  connectSse() {
    this.eventSource = new EventSource(`${environment.sseApiEndpoint}`,
      {withCredentials: true})

    this.eventSource.addEventListener('stream-status', (event: MessageEvent) => {
      const data: StreamerStateEvent = JSON.parse(event.data);
      this.updateStreamerState(data);
    });
  }

  updateStreamerState(state: StreamerStateEvent) {
    this.streamers.update(list =>
    list.map(s =>
    s.streamerId === state.streamerId
      ? {...s, live: state.live, recordingStatus: state.recordingStatus}
      : s)
    );
  };

  reconnectSse() {
    if (this.eventSource) {
      this.eventSource.close();
    }
    this.connectSse();
  }

  get streamersState() {
    return this.streamers.asReadonly();
  }
}
