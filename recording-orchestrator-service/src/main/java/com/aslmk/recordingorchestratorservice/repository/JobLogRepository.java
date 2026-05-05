package com.aslmk.recordingorchestratorservice.repository;

import com.aslmk.recordingorchestratorservice.domain.JobLogEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobLogRepository extends CrudRepository<JobLogEntity, UUID> {
    @Modifying
    @Query(value = """
          UPDATE job_logs
          SET status = :status, updated_at = now()
          WHERE id = :id;
          """, nativeQuery = true)
    void updateStatus(@Param("id") UUID id, String status);

    @Query(value = """
          SELECT * FROM job_logs
          WHERE status = 'PENDING'
          LIMIT 50;
          """, nativeQuery = true)
    List<JobLogEntity> getAllPendingJobs();
}
