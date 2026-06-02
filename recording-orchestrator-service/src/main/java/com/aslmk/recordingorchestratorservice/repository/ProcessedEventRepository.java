package com.aslmk.recordingorchestratorservice.repository;

import com.aslmk.recordingorchestratorservice.domain.ProcessedEventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventRepository extends CrudRepository<ProcessedEventEntity, UUID> {}
