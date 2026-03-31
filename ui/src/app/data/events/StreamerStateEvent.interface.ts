import {RecordingStatus} from '../enums/RecordingStatus.enum';

export interface StreamerStateEvent {
  streamerId: string,
  live: boolean,
  recordingStatus: RecordingStatus
}
