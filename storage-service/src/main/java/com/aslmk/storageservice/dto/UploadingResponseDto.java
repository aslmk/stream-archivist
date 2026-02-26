package com.aslmk.storageservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadingResponseDto {
    private String uploadId;
    private List<String> uploadURLs;
}
