import {Streamer} from './streamer.interface';
import {RecordingStatus} from '../enums/RecordingStatus.enum';

export interface StreamerView extends Streamer {
  live: boolean,
  recordingStatus: RecordingStatus
}
