package com.aslmk.streamstatusservice.service.impl;

import com.aslmk.streamstatusservice.entity.StreamerKey;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamerIdentityResolver {
    private final Map<StreamerKey, UUID> streamerIdentity = new ConcurrentHashMap<>();

    public UUID resolve(StreamerKey streamerKey) {
        return streamerIdentity.computeIfAbsent(streamerKey, k -> UUID.randomUUID());
    }
}
