package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.UploadingSessionData;

import java.util.Optional;
import java.util.UUID;

public interface UploadSessionService {
    Optional<UploadSessionEntity> findByUploadId(String uploadId);
    Optional<UploadSessionEntity> findByStreamId(UUID streamId);
    void saveIfNotExists(UploadingSessionData data);
}
