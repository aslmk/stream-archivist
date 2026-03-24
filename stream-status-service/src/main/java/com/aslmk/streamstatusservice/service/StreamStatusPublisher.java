package com.aslmk.streamstatusservice.service;

import com.aslmk.streamstatusservice.domain.StreamState;

import java.util.Set;
import java.util.UUID;

public interface StreamStatusPublisher {
    void publish(StreamState status, UUID streamerId, Set<UUID> userIds);
}
