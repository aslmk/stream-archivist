package com.aslmk.recordingorchestratorservice.repository;

import com.aslmk.recordingorchestratorservice.domain.RecordedFilePartEntity;
import com.aslmk.recordingorchestratorservice.domain.RecordedFilePartId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordedFilePartRepository extends CrudRepository<RecordedFilePartEntity, RecordedFilePartId> {

}
