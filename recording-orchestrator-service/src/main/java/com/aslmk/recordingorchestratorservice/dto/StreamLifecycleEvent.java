package com.aslmk.recordingorchestratorservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamLifecycleEvent {
    private UUID eventId;
    private StreamLifecycleType eventType;
    private String streamerUsername;
    private String streamUrl;
    private UUID streamerId;
}
