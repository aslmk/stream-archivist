package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.dto.InitMultipartUploadDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;

public interface StorageRepository {
    UploadingResponseDto initiateUpload(InitMultipartUploadDto dto);
}
