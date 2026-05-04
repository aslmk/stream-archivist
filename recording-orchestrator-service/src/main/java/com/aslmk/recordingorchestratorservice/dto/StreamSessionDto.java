package com.aslmk.recordingorchestratorservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamSessionDto {
    private UUID streamerId;
    private StreamSessionStatus status;
}
