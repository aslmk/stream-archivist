package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.RecordedFilePartEntity;
import com.aslmk.recordingorchestratorservice.domain.RecordedFilePartId;
import com.aslmk.recordingorchestratorservice.dto.RecordedPartDto;
import com.aslmk.recordingorchestratorservice.repository.RecordedFilePartRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class RecordedFilePartServiceImpl implements RecordedFilePartService {

    private final RecordedFilePartRepository repository;

    public RecordedFilePartServiceImpl(RecordedFilePartRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean saveIfNotExist(RecordedPartDto dto) {
        try {
            RecordedFilePartEntity entity = RecordedFilePartEntity.builder()
                    .id(RecordedFilePartId.builder()
                            .streamId(dto.getStreamId())
                            .partIndex(dto.getPartIndex())
                            .build())
                    .filePartPath(dto.getFilePartPath())
                    .filePartName(dto.getFilePartName())
                    .build();

            repository.save(entity);
            return true;
        }catch (DataIntegrityViolationException e){
            if (e.getCause() instanceof ConstraintViolationException) {
                return false;
            }
            throw e;
        }
    }
}
