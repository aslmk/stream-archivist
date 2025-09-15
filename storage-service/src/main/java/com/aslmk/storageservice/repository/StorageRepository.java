package com.aslmk.storageservice.repository;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;

public interface StorageRepository {
    UploadingResponseDto initiateUpload(InitMultipartUploadDto dto);
    void completeUpload(CompleteMultipartUploadRequest request);
}
