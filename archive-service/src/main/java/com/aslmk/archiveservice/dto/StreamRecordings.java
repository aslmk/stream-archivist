package com.aslmk.archiveservice.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamRecordings {
    private UUID streamerId;
    private List<RecordingDownloads> recordings;
}
