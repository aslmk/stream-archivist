package com.aslmk.streamstatusservice.controller;

import com.aslmk.streamstatusservice.constant.GatewayHeaders;
import com.aslmk.streamstatusservice.domain.StreamState;
import com.aslmk.streamstatusservice.service.StreamStatusOrchestrator;
import com.aslmk.streamstatusservice.registry.StreamStatusRegistry;
import com.aslmk.streamstatusservice.service.StreamStatusSsePublisher;
import com.aslmk.streamstatusservice.registry.SubscriptionsRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
public class SseStreamStatusController {

    private final StreamStatusSsePublisher publisher;
    private final StreamStatusOrchestrator orchestrator;
    private final SubscriptionsRegistry subscriptionsRegistry;
    private final StreamStatusRegistry streamStatusRegistry;

    public SseStreamStatusController(StreamStatusSsePublisher publisher,
                                     StreamStatusOrchestrator orchestrator,
                                     SubscriptionsRegistry subscriptionsRegistry,
                                     StreamStatusRegistry streamStatusRegistry) {
        this.publisher = publisher;
        this.orchestrator = orchestrator;
        this.subscriptionsRegistry = subscriptionsRegistry;
        this.streamStatusRegistry = streamStatusRegistry;
    }

    @GetMapping(value = "/stream-status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamState>> streamStatus(
            @RequestHeader(GatewayHeaders.USER_ID) String userId) {
        UUID uuidUserId = UUID.fromString(userId);

        return orchestrator.registerUser(uuidUserId)
                .thenMany(
                        Flux.fromIterable(subscriptionsRegistry.getOrCreateUserSubscriptions(uuidUserId))
                                .map(streamStatusRegistry::getOrCreate)
                )
                .concatWith(publisher.register(uuidUserId))
                .map(status -> ServerSentEvent.builder(status)
                        .event("stream-status")
                        .build())
                .doOnCancel(() -> publisher.unregister(uuidUserId));
    }
}
