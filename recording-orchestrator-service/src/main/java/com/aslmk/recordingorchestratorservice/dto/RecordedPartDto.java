package com.aslmk.recordingorchestratorservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordedPartDto {
    private UUID streamId;
    private String filePartName;
    private String filePartPath;
    private Integer partIndex;
}
