package com.aslmk.recordingorchestratorservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordStreamJob {
    private String streamUrl;
    private String streamerUsername;
    private UUID streamId;
}
