package com.aslmk.uploadingworker.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadingResponseDto {
    private String uploadId;
    private Map<Integer, String> uploadURLs;
    private boolean hasNext;
    private Integer nextPartNumberMarker;
}
