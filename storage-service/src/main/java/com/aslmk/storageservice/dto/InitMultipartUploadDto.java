package com.aslmk.storageservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitMultipartUploadDto {
    private String s3ObjectPath;
    private Integer fileParts;
}
