package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.dto.UploadPartsInfo;

public interface StorageRepository {
    String generateUploadId(String objectKey);
    UploadPartsInfo getUploadPart(String uploadId, String objectKey,
                                  Integer partNumberMarker, int expectedParts);
}
