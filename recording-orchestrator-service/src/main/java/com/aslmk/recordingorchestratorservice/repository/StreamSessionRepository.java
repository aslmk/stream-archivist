package com.aslmk.recordingorchestratorservice.repository;


import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamSessionRepository extends CrudRepository<StreamSessionEntity, UUID> {
    Optional<StreamSessionEntity> findByStreamId(UUID streamId);

    @Modifying
    @Query(value = """
                   UPDATE stream_sessions
                   SET status = :newStatus, updated_at = now()
                   WHERE stream_id = :id;
                   """,
            nativeQuery = true)
    void updateStatus(@Param("id") UUID streamId, @Param("newStatus") String newStatus);
}
