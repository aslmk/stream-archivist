package com.aslmk.streamstatusservice.service.impl;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SubscriptionsRegistry {
    private final Map<UUID, Set<UUID>> streamerSubscriptions = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> userSubscriptions = new ConcurrentHashMap<>();


    public Set<UUID> getOrCreateStreamerSubscriptions(UUID streamerId) {
        return streamerSubscriptions.computeIfAbsent(streamerId, k -> ConcurrentHashMap.newKeySet());
    }

    public Set<UUID> getOrCreateUserSubscriptions(UUID userId) {
        return userSubscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
    }
}
