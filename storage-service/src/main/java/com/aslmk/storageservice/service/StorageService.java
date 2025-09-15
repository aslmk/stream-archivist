package com.aslmk.storageservice.service;

import com.aslmk.common.dto.UploadCompletedEvent;
import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;

public interface StorageService {
    UploadingResponseDto initiateUpload(UploadingRequestDto request);
    void completeUpload(UploadCompletedEvent uploadCompletedEvent);
}
