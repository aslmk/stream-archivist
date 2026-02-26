package com.aslmk.uploadingworker.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordingStatusEvent {
    private RecordingEventType eventType;
    private String streamerUsername;
    private String filename;
    private UUID streamerId;
}
