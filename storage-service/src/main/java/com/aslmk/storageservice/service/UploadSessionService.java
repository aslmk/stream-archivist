package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.UploadingSessionData;

import java.util.Optional;

public interface UploadSessionService {
    Optional<UploadSessionEntity> findByS3ObjectPath(String s3ObjectPath);
    Optional<UploadSessionEntity> findByUploadId(String uploadId);
    void saveIfNotExists(UploadingSessionData data);
}
