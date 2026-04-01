import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserSubscriptions} from '../interfaces/UserSubscriptions.interface';
import {map} from 'rxjs';
import {Streamer} from '../interfaces/Streamer.interface';
import {RecordingStatus} from '../enums/RecordingStatus.enum';
import {StreamerView} from '../interfaces/StreamerView.interface';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root',
})

export class StreamerService {
  private http = inject(HttpClient)

  getTrackedStreamers() {
    return this.http.get<UserSubscriptions>(`${environment.subscriptionsApiEndpoint}`,
      {withCredentials: true})
      .pipe(
      map(response => response.userSubscriptions),
      map(streamers => streamers.map(s => this.mapToView(s)))
    )
  }

  subscribeToStreamer(streamerUsername: string, providerName: string) {
    return this.http.post(`${environment.subscriptionsApiEndpoint}`,
      {streamerUsername: streamerUsername, providerName: providerName},
      {withCredentials: true});
  }

  unsubscribeFromStreamer(streamerId: string) {
    return this.http.delete(`${environment.subscriptionsApiEndpoint}?streamerId=`+streamerId,
      { withCredentials: true });
  }

  private mapToView(streamer: Streamer): StreamerView {
    return {
      ...streamer,
      live: false,
      recordingStatus: RecordingStatus.NOT_RECORDING
    };
  }
}
