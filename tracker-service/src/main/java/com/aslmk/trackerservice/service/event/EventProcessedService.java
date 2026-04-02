package com.aslmk.trackerservice.service.event;

public interface EventProcessedService {
    boolean tryMarkAsProcessed(String eventId);
}
