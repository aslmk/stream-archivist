package com.aslmk.archiveservice.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingDownloadRequest {
    private List<UUID> streamIds;
}
