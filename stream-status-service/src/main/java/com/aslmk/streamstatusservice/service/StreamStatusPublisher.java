package com.aslmk.streamstatusservice.service;

import com.aslmk.streamstatusservice.domain.StreamState;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.UUID;

public interface StreamStatusPublisher {
    Flux<StreamState> register(UUID userId);
    void publish(StreamState status, UUID streamerId, Set<UUID> userIds);
    void unregister(UUID userId);
}
