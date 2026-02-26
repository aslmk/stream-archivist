package com.aslmk.uploadingworker.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartUploadResultDto {
    private int partNumber;
    private String etag;
}
