package com.aslmk.storageservice.service;

import com.aslmk.storageservice.dto.InitUploadingRequest;
import com.aslmk.storageservice.dto.InitUploadingResponse;
import com.aslmk.storageservice.dto.UploadPartsInfo;

public interface StorageService {
    InitUploadingResponse initUpload(InitUploadingRequest request);
    UploadPartsInfo getParts(String uploadId, Integer partNumberMarker);
}
