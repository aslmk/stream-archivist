package com.aslmk.recordingorchestratorservice.service;

import java.util.UUID;

public interface ProcessedEventService {
    boolean tryMarkAsProcessed(UUID eventId);
}
