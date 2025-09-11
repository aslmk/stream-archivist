package com.aslmk.uploadingworker.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3PartDto {
    private String preSignedUrl;
    private byte[] partData;
}
