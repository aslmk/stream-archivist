package com.aslmk.storageservice.repository;

import com.aslmk.storageservice.domain.StreamSessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamSessionRepository extends CrudRepository<StreamSessionEntity, UUID> {
    Optional<StreamSessionEntity> findByStreamId(UUID streamId);
    void removeByStreamId(UUID streamId);
}
