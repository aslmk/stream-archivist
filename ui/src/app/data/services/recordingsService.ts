import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';
import {StreamRecordings} from '../interfaces/StreamRecordings';

@Injectable({
  providedIn: 'root',
})
export class RecordingsService {
  private http = inject(HttpClient);

  getByStreamerId(streamerId: string) {
    return this.http.get<StreamRecordings>(`${environment.archiveEndpoint}/` + streamerId,
      {withCredentials: true});
  }
}
