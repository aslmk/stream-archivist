import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserSubscriptions} from '../interfaces/userSubscriptions.interface';
import {map} from 'rxjs';
import {Streamer} from '../interfaces/streamer.interface';
import {RecordingStatus} from '../enums/RecordingStatus.enum';
import {StreamerView} from '../interfaces/StreamerView.interface';

@Injectable({
  providedIn: 'root',
})

export class StreamerService {
  http = inject(HttpClient)

  getTrackedStreamers() {
    return this.http.get<UserSubscriptions>('/api/v1/subscriptions', {withCredentials: true})
      .pipe(
      map(response => response.userSubscriptions),
      map(streamers => streamers.map(s => this.mapToView(s)))
    )
  }

  mapToView(streamer: Streamer): StreamerView {
    return {
      ...streamer,
      isLive: false,
      recordingStatus: RecordingStatus.Not_Recording
    };
  }
}
