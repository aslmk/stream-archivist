package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.EntityIdResolveResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionServiceClient {
    List<EntityIdResolveResponse> getTrackedStreamers(UUID userId);
}
