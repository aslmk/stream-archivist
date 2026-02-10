package com.aslmk.streamstatusservice.service;

import com.aslmk.streamstatusservice.entity.StreamStatusEntity;

import java.util.Set;
import java.util.UUID;

public interface StreamStatusPublisher {
    void publish(StreamStatusEntity status, UUID streamerId, Set<UUID> userIds);
}
