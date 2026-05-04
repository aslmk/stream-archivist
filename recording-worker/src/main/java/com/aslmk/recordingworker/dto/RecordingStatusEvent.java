package com.aslmk.recordingworker.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordingStatusEvent {
    private RecordingEventType eventType;
    private String filename;
    private UUID streamId;
}
