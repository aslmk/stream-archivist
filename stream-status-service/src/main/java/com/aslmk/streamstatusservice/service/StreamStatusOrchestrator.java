package com.aslmk.streamstatusservice.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StreamStatusOrchestrator {
    Mono<Void> registerUser(UUID userId);
}
