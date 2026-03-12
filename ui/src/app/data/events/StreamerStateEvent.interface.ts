import {RecordingStatus} from '../enums/RecordingStatus.enum';

export interface StreamerStateEvent {
  streamerId: string,
  isLive: boolean,
  recordingStatus: RecordingStatus
}
