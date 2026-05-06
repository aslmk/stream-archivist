package com.aslmk.trackerservice.service.event;

import com.aslmk.trackerservice.domain.EventLogEntity;
import com.aslmk.trackerservice.domain.EventType;
import com.aslmk.trackerservice.dto.EventLogStatus;
import com.aslmk.trackerservice.dto.EventPayload;
import com.aslmk.trackerservice.repository.EventLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EventLogServiceImpl implements EventLogService {

    private final EventLogRepository repository;

    public EventLogServiceImpl(EventLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void updateStatus(UUID eventId, EventLogStatus eventStatus) {
        repository.updateStatus(eventId, eventStatus.name());
    }

    @Override
    public List<EventLogEntity> getAllPendingEvents() {
        return repository.getAllPendingEvents();
    }

    @Override
    public void save(EventPayload payload, EventType eventType) {
        EventLogEntity entity = EventLogEntity.builder()
                .status(EventLogStatus.PENDING.name())
                .eventType(eventType.name())
                .payload(payload)
                .build();

        repository.save(entity);
    }
}
