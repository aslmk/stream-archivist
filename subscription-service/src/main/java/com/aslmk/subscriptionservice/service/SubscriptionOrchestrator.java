package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.UserRef;

import java.util.UUID;

public interface SubscriptionOrchestrator {
    void subscribe(UserRef userRef, StreamerRef streamerRef);
    void unsubscribe(UUID userId, UUID streamerId);
}
