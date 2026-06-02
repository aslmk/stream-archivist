package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.ProcessedEventEntity;
import com.aslmk.recordingorchestratorservice.repository.ProcessedEventRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProcessedEventServiceImpl implements ProcessedEventService {
    private final ProcessedEventRepository repository;

    public ProcessedEventServiceImpl(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean tryMarkAsProcessed(UUID eventId) {
        try {
            ProcessedEventEntity entity = ProcessedEventEntity.builder()
                    .id(eventId)
                    .build();

            repository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                return false;
            }
            throw e;
        }
    }
}
