package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;

import java.util.Optional;

public interface UploadSessionService {
    Optional<UploadSessionEntity> findByS3ObjectPath(String s3ObjectPath);
    void saveIfNotExists(String s3ObjectPath, String uploadId);
}
