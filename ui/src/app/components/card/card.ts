import {Component, ElementRef, HostListener, inject, Input, OnChanges, ViewChild} from '@angular/core';
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
export class Card implements OnChanges {

  textState!: string;
  dotState!: string;

  subscriptionService = inject(SubscriptionService);

  twitchIconUrl = environment.twitchIconUrl;
  menuOpen = false;
  @ViewChild('menuRoot') menuRoot!: ElementRef;

  @Input() streamer !: StreamerView;

  ngOnChanges(): void {
    this.textState = this.computeTextState();
    this.dotState = this.computeDotState();
  }

  private computeTextState(): string {
    switch (this.streamer.recordingStatus) {
      case RecordingStatus.RECORDING:
        return 'text-red-600';

      case RecordingStatus.NOT_RECORDING:
        return 'text-yellow-600';

      case RecordingStatus.FAILED:
        return 'text-orange-600';

      case RecordingStatus.FINISHED:
        return 'text-green-600';

      default:
        return 'text-white';
    }
  }

  private computeDotState(): string {
    switch (this.streamer.recordingStatus) {
      case RecordingStatus.RECORDING:
        return 'bg-red-600';

      case RecordingStatus.NOT_RECORDING:
        return 'bg-yellow-600';

      case RecordingStatus.FAILED:
        return 'bg-orange-600';

      case RecordingStatus.FINISHED:
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

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.menuOpen) return;

    const clickedInside = this.menuRoot?.nativeElement.contains(event.target);

    if (!clickedInside) {
      this.menuOpen = false;
    }
  }

}
