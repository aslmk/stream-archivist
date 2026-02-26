package com.aslmk.storageservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadingRequestDto {
    private String streamerUsername;
    private String fileName;
    private Integer fileParts;
}
