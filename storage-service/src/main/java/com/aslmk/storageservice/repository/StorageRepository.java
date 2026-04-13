package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.dto.MultipartUploadDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;

public interface StorageRepository {
    UploadingResponseDto processUpload(MultipartUploadDto dto);
}
