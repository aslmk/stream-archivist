package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.StreamSessionEntity;
import com.aslmk.storageservice.dto.StreamSessionData;

import java.util.Optional;
import java.util.UUID;

public interface StreamSessionService {
    void saveIfNotExists(StreamSessionData data);
    Optional<StreamSessionEntity> getByStreamId(UUID streamId);
    String getUploadId(UUID streamId);
    void removeByStreamId(UUID streamId);
}
