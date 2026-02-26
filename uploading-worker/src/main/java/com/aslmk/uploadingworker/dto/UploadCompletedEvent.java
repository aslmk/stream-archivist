package com.aslmk.uploadingworker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadCompletedEvent {
    private List<PartUploadResultDto> partUploadResults;
    private String filename;
    private String uploadId;
    private String streamerUsername;
}
