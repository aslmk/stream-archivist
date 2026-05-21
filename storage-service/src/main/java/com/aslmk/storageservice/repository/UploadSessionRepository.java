package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadSessionRepository extends CrudRepository<UploadSessionEntity, UUID> {
    Optional<UploadSessionEntity> findByUploadId(String uploadId);
    Optional<UploadSessionEntity> findByStreamId(UUID streamId);
}
