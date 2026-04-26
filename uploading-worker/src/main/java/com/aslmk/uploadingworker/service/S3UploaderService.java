package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.dto.UploadRecordedPart;

public interface S3UploaderService {
    void upload(S3UploadRequestDto request);
    void uploadPart(UploadRecordedPart part);
}
