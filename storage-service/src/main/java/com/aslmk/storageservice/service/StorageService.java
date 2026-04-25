package com.aslmk.storageservice.service;

import com.aslmk.storageservice.dto.*;

public interface StorageService {
    InitUploadingResponse initUpload(InitUploadingRequest request);
    UploadPartsInfo getParts(String uploadId, Integer partNumberMarker);
    void initChunkedUpload(InitChunkedUpload init);
    PreSignedUrl getPreSignedUrl(RecordedPartInfo part);
    void completeChunkedUpload(CompleteChunkedUpload complete);
}
