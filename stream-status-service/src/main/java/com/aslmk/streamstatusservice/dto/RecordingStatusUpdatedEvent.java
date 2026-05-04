package com.aslmk.streamstatusservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingStatusUpdatedEvent {
    private UUID streamerId;
    private RecordingEventType eventType;
}
