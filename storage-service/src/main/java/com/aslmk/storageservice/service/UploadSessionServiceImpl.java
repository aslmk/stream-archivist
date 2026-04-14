package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.UploadingSessionData;
import com.aslmk.storageservice.repository.UploadSessionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UploadSessionServiceImpl implements UploadSessionService {
    private final UploadSessionRepository repository;

    public UploadSessionServiceImpl(UploadSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UploadSessionEntity> findByS3ObjectPath(String s3ObjectPath) {
        return repository.findByS3ObjectPath(s3ObjectPath);
    }

    @Override
    public void saveIfNotExists(UploadingSessionData data) {
        try {
            UploadSessionEntity entity = UploadSessionEntity.builder()
                    .s3ObjectPath(data.objectKey())
                    .uploadId(data.uploadId())
                    .expectedParts(data.expectedParts())
                    .build();

            repository.save(entity);
        } catch (DataIntegrityViolationException ignored) {}
    }

    @Override
    public Optional<UploadSessionEntity> findByUploadId(String uploadId) {
        return repository.findByUploadId(uploadId);
    }
}
