package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadSessionRepository extends CrudRepository<UploadSessionEntity, String> {
    Optional<UploadSessionEntity> findByS3ObjectPath(String s3ObjectPath);
}
