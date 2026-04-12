package com.aslmk.uploadingworker.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3UploadRequestDto {
    private String filePath;
    private Map<Integer, String> uploadUrls;
    private List<FilePart> fileParts;
}
