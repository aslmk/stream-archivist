package com.aslmk.recordingorchestratorservice.dto;

import com.aslmk.recordingorchestratorservice.domain.JobPayload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordStreamJob implements JobPayload {
    private String streamUrl;
    private String streamerUsername;
    private UUID streamId;
}
