package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.EntityIdResolveResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface SubscriptionServiceClient {
    Mono<List<EntityIdResolveResponse>> getTrackedStreamers(UUID userId);
}
