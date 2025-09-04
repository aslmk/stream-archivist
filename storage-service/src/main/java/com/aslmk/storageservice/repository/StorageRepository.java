package com.aslmk.storageservice.repository;

import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;

public interface StorageRepository {
    UploadingResponseDto initiateUpload(InitMultipartUploadDto dto);
}
