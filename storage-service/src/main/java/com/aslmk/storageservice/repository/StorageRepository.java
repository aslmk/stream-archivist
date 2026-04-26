package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.dto.PreSignedUrl;
import com.aslmk.storageservice.dto.UploadPartsInfo;

public interface StorageRepository {
    String generateUploadId(String objectKey);
    PreSignedUrl generatePreSignedUrl(String uploadId, Long partNumber, String objectKey);
    UploadPartsInfo getUploadPart(String uploadId, String objectKey,
                                  Integer partNumberMarker, int expectedParts);
    void completeChunkedUpload(String uploadId, String key);
}
