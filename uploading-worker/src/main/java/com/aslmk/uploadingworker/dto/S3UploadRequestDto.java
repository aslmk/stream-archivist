package com.aslmk.uploadingworker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3UploadRequestDto {
    private String filePath;
    private List<String> uploadUrls;
    private List<FilePart> fileParts;
}
