package com.aslmk.streamstatusservice.client;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthServiceClient {
    Mono<UUID> resolveUserId(String providerUserId, String providerName);
}
