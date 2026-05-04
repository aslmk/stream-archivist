package com.aslmk.recordingorchestratorservice.repository;


import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StreamSessionRepository extends CrudRepository<StreamSessionEntity, UUID> {


}
