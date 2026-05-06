package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.domain.EventLogEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventLogRepository extends CrudRepository<EventLogEntity, UUID> {

    @Modifying
    @Query(value = """
          UPDATE event_logs
          SET status = :status, updated_at = now()
          WHERE id = :id;
          """, nativeQuery = true)
    void updateStatus(@Param("id") UUID id, String status);

    @Query(value = """
          SELECT * FROM event_logs
          WHERE status = 'PENDING'
          LIMIT 50;
          """, nativeQuery = true)
    List<EventLogEntity> getAllPendingEvents();
}
