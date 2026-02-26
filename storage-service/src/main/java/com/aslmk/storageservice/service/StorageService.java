package com.aslmk.storageservice.service;

import com.aslmk.storageservice.dto.UploadCompletedEvent;
import com.aslmk.storageservice.dto.UploadingRequestDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;

public interface StorageService {
    UploadingResponseDto initiateUpload(UploadingRequestDto request);
    void completeUpload(UploadCompletedEvent uploadCompletedEvent);
}
