package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.TrackedStreamerDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface SubscriptionServiceClient {
    Mono<List<TrackedStreamerDto>> getTrackedStreamers(UUID userId);
}
