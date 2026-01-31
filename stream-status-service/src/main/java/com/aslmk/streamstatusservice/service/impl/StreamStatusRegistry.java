package com.aslmk.streamstatusservice.service.impl;

import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamStatusRegistry {
    private final Map<UUID, StreamStatusEntity> statuses = new ConcurrentHashMap<>();

    public StreamStatusEntity getOrCreate(UUID streamerId) {
        return statuses.computeIfAbsent(streamerId, StreamStatusEntity::new);
    }
}
