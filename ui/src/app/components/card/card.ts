import {Component, inject, Input} from '@angular/core';
import {ImgUrlPipe} from '../../helpers/pipes/img-url-pipe';
import {StreamerView} from '../../data/interfaces/StreamerView.interface';
import {NgClass} from '@angular/common';
import {environment} from '../../../environments/environments';
import {RecordingStatus} from '../../data/enums/RecordingStatus.enum';
import {SubscriptionService} from '../../data/services/subscriptionService';

@Component({
  selector: 'app-card',
  imports: [
    ImgUrlPipe,
    NgClass
  ],
  templateUrl: './card.html',
  styleUrl: './card.css',
  standalone: true
})
export class Card {

  subscriptionService = inject(SubscriptionService);

  twitchIconUrl = environment.twitchIconUrl;
  menuOpen = false;

  @Input() streamer !: StreamerView;

  get textState(): string {
    switch (this.streamer.recordingStatus) {
      case RecordingStatus.Recording:
        return 'text-red-600';

      case RecordingStatus.Not_Recording:
        return 'text-yellow-600';

      case RecordingStatus.Failed:
        return 'text-orange-600';

      case RecordingStatus.Finished:
        return 'text-green-600';

      default:
        return 'text-white';
    }
  }

  get dotState(): string {
    switch (this.streamer.recordingStatus) {
      case RecordingStatus.Recording:
        return 'bg-red-600';

      case RecordingStatus.Not_Recording:
        return 'bg-yellow-600';

      case RecordingStatus.Failed:
        return 'bg-orange-600';

      case RecordingStatus.Finished:
        return 'bg-green-600';

      default:
        return 'bg-white';
    }
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  onUnsubscribe(streamerId: string) {
    this.menuOpen = false;
    this.subscriptionService.unsubscribe(streamerId);
  }

}
