package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
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
    public void saveIfNotExists(String s3ObjectPath, String uploadId) {
        try {
            UploadSessionEntity entity = UploadSessionEntity.builder()
                    .s3ObjectPath(s3ObjectPath)
                    .uploadId(uploadId)
                    .build();

            repository.save(entity);
        } catch (DataIntegrityViolationException ignored) {}
    }
}
