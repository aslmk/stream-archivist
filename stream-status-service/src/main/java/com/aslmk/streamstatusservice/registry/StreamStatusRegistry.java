package com.aslmk.streamstatusservice.registry;

import com.aslmk.streamstatusservice.domain.StreamState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamStatusRegistry {
    private final Map<UUID, StreamState> statuses = new ConcurrentHashMap<>();

    public StreamState getOrCreate(UUID streamerId) {
        return statuses.computeIfAbsent(streamerId, StreamState::new);
    }
}
