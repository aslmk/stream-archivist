package com.aslmk.streamstatusservice.controller;

import com.aslmk.common.constants.GatewayHeaders;
import com.aslmk.streamstatusservice.client.AuthServiceClient;
import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import com.aslmk.streamstatusservice.service.StreamStatusOrchestrator;
import com.aslmk.streamstatusservice.service.impl.StreamStatusRegistry;
import com.aslmk.streamstatusservice.service.impl.StreamStatusSsePublisher;
import com.aslmk.streamstatusservice.service.impl.SubscriptionsRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/sse")
public class SseStreamStatusController {

    private final StreamStatusSsePublisher publisher;
    private final StreamStatusOrchestrator orchestrator;
    private final AuthServiceClient authServiceClient;
    private final SubscriptionsRegistry subscriptionsRegistry;
    private final StreamStatusRegistry streamStatusRegistry;

    public SseStreamStatusController(StreamStatusSsePublisher publisher, StreamStatusOrchestrator orchestrator, AuthServiceClient authServiceClient, SubscriptionsRegistry subscriptionsRegistry, StreamStatusRegistry streamStatusRegistry) {
        this.publisher = publisher;
        this.orchestrator = orchestrator;
        this.authServiceClient = authServiceClient;
        this.subscriptionsRegistry = subscriptionsRegistry;
        this.streamStatusRegistry = streamStatusRegistry;
    }

    @GetMapping(value = "/stream-status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamStatusEntity>> streamStatus(@RequestHeader(GatewayHeaders.USER_ID) String providerUserId,
                                                 @RequestHeader(GatewayHeaders.PROVIDER_NAME) String providerName) {
        return authServiceClient.resolveUserId(providerUserId, providerName)
                .flatMapMany(userId ->
                    orchestrator.registerUser(userId)
                            .thenMany(
                                    Flux.fromIterable(subscriptionsRegistry.getOrCreateUserSubscriptions(userId))
                                            .map(streamStatusRegistry::getOrCreate)
                            )
                            .concatWith(publisher.register(userId))
                            .map(status -> ServerSentEvent.builder(status)
                                    .event("stream-status")
                                    .build())
                            .doOnCancel(() -> publisher.unregister(userId))
                );
    }
}
