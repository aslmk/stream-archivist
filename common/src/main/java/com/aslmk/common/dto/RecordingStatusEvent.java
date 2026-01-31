package com.aslmk.common.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordingStatusEvent {
    private RecordingEventType eventType;
    private String streamerUsername;
    private String filename;
    private String providerName;
    private String providerUserId;
}
