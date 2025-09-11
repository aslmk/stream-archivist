package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.dto.S3UploadRequestDto;

public interface S3UploaderService {

    void upload(S3UploadRequestDto request);
}
