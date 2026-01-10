package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.UserRef;

public interface SubscriptionOrchestrator {
    void subscribe(UserRef userRef, StreamerRef streamerRef);
}
