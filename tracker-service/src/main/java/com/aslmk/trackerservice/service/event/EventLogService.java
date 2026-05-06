package com.aslmk.trackerservice.service.event;

import com.aslmk.trackerservice.domain.EventLogEntity;
import com.aslmk.trackerservice.domain.EventType;
import com.aslmk.trackerservice.dto.EventLogStatus;
import com.aslmk.trackerservice.dto.EventPayload;

import java.util.List;
import java.util.UUID;

public interface EventLogService {
    void updateStatus(UUID eventId, EventLogStatus eventStatus);
    List<EventLogEntity> getAllPendingEvents();
    void save(EventPayload payload, EventType eventType);
}
