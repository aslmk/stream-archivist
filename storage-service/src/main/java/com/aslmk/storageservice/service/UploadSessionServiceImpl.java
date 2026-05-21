package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.UploadingSessionData;
import com.aslmk.storageservice.repository.UploadSessionRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UploadSessionServiceImpl implements UploadSessionService {
    private final UploadSessionRepository repository;

    public UploadSessionServiceImpl(UploadSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveIfNotExists(UploadingSessionData data) {
        try {
            UploadSessionEntity entity = UploadSessionEntity.builder()
                    .streamId(data.streamId())
                    .s3ObjectKey(data.objectKey())
                    .uploadId(data.uploadId())
                    .expectedParts(data.expectedParts())
                    .build();

            repository.save(entity);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                return;
            }

            throw e;
        }
    }

    @Override
    public Optional<UploadSessionEntity> findByUploadId(String uploadId) {
        return repository.findByUploadId(uploadId);
    }

    @Override
    public Optional<UploadSessionEntity> findByStreamId(UUID streamId) {
        return repository.findByStreamId(streamId);
    }
}
