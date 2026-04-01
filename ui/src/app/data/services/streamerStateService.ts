import {inject, Injectable, signal} from '@angular/core';
import {StreamerView} from '../interfaces/StreamerView.interface';
import {StreamerService} from './streamerService';
import {StreamerStateEvent} from '../events/StreamerStateEvent.interface';
import {environment} from '../../../environments/environments';
import {AuthService} from './authService';

@Injectable({
  providedIn: 'root',
})
export class StreamerStateService {
  private streamerService = inject(StreamerService)
  private authService = inject(AuthService);
  private streamers = signal<StreamerView[]>([])
  private eventSource?: EventSource;

  constructor() {
    this.authService.onRefresh().subscribe(() => {
      this.reconnectSse();
    });
  }

  getTrackedStreamers() {
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
    this.disconnectSse()
    this.connectSse();
  }

  disconnectSse() {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }

  get streamersState() {
    return this.streamers.asReadonly();
  }
}
