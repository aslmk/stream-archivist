import {inject, Injectable, signal} from '@angular/core';
import {StreamerView} from '../interfaces/StreamerView.interface';
import {StreamerService} from './streamerService';
import {StreamerStateEvent} from '../events/StreamerStateEvent.interface';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root',
})
export class StreamerStateService {
  streamerService = inject(StreamerService)

  streamers = signal<StreamerView[]>([])

  constructor() {}

  loadInitial() {
    this.streamerService.getTrackedStreamers()
      .subscribe(val =>
        this.streamers.set(val));
  }

  connectSse() {
    const eventSource = new EventSource(`${environment.sseApiEndpoint}`,
      {withCredentials: true})
    eventSource.onmessage = (event) => {
      const data: StreamerStateEvent = JSON.parse(event.data);
      this.updateStreamerState(data);
    }
  }

  updateStreamerState(state: StreamerStateEvent) {
    this.streamers.update(list =>
    list.map(s =>
    s.streamerId === state.streamerId
      ? {...s, isLive: state.isLive, recordingStatus: state.recordingStatus}
      : s)
    );
  };
}
