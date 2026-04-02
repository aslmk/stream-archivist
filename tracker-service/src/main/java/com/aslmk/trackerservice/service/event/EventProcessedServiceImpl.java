package com.aslmk.trackerservice.service.event;

import com.aslmk.trackerservice.domain.EventProcessedEntity;
import com.aslmk.trackerservice.repository.EventProcessedRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class EventProcessedServiceImpl implements EventProcessedService {

    private final EventProcessedRepository repository;

    public EventProcessedServiceImpl(EventProcessedRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean tryMarkAsProcessed(String eventId) {
        try {
            EventProcessedEntity entity = EventProcessedEntity.builder()
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
