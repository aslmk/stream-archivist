package com.aslmk.recordingorchestratorservice.dto;

import com.aslmk.recordingorchestratorservice.domain.JobPayload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadStreamRecordJob implements JobPayload {
    private String filename;
    private UUID streamId;
}
