package com.aslmk.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecordingStatusEvent {
    private RecordingEventType eventType;
    private String streamerUsername;
    private String filename;
    private String providerName;
    private String providerUserId;
}
