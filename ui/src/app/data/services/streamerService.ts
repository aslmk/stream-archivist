import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserSubscriptions} from '../interfaces/userSubscriptions.interface';
import {map} from 'rxjs';
import {Streamer} from '../interfaces/streamer.interface';
import {RecordingStatus} from '../enums/RecordingStatus.enum';
import {StreamerView} from '../interfaces/StreamerView.interface';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root',
})

export class StreamerService {
  http = inject(HttpClient)

  getTrackedStreamers() {
    return this.http.get<UserSubscriptions>(`${environment.subscriptionsApiEndpoint}`,
      {withCredentials: true})
      .pipe(
      map(response => response.userSubscriptions),
      map(streamers => streamers.map(s => this.mapToView(s)))
    )
  }

  unsubscribeFromStreamer(streamerId: string) {
    return this.http.delete(`${environment.subscriptionsApiEndpoint}?streamerId=`+streamerId,
      { withCredentials: true });
  }

  mapToView(streamer: Streamer): StreamerView {
    return {
      ...streamer,
      live: false,
      recordingStatus: RecordingStatus.NOT_RECORDING
    };
  }
}
