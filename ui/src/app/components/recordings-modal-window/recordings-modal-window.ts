import {Component, EventEmitter, inject, Input, OnInit, Output, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Recording} from '../../data/interfaces/Recording';
import {RecordingsService} from '../../data/services/recordingsService';

@Component({
  selector: 'app-recordings-modal-window',
  imports: [
    DatePipe
  ],
  templateUrl: './recordings-modal-window.html',
  styleUrl: './recordings-modal-window.css',
  standalone: true
})
export class RecordingsModalWindow implements OnInit {
  @Input({ required: true }) streamerId !: string;
  @Output() close = new EventEmitter<void>();

  recordings = signal<Recording[]>([]);
  isLoading = signal(true);
  hasError = signal(false);
  errorMessage = signal('');
  readonly skeletonItems = Array(4).fill(null);

  recordingsService = inject(RecordingsService);

  ngOnInit(): void {
    this.loadRecordings();
  }

  closeModal() {
    this.close.emit();
  }

  loadRecordings(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.errorMessage.set('');

    this.recordingsService.getByStreamerId(this.streamerId)
      .subscribe({
        next: (data) => {
          this.recordings.set(data.recordings);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.hasError.set(true);
          this.errorMessage.set(err?.message ?? 'Unknown error');
          this.isLoading.set(false);
        }
      });
  }
}
