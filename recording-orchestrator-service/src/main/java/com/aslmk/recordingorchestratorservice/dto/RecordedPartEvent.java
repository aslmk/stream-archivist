package com.aslmk.recordingorchestratorservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordedPartEvent {
    private RecordedPartEventType eventType;
    private String filePartPath;
    private String filePartName;
    private UUID streamId;
    private int partIndex;

}
