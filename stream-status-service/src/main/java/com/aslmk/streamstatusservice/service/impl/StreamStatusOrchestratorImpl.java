package com.aslmk.streamstatusservice.service.impl;

import com.aslmk.streamstatusservice.client.SubscriptionServiceClient;
import com.aslmk.streamstatusservice.service.StreamStatusOrchestrator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class StreamStatusOrchestratorImpl implements StreamStatusOrchestrator {

    private final SubscriptionServiceClient subscriptionServiceClient;
    private final StreamStatusRegistry streamStatusRegistry;
    private final SubscriptionsRegistry subscriptionRegistry;

    public StreamStatusOrchestratorImpl(SubscriptionServiceClient subscriptionServiceClient,
                                        StreamStatusRegistry streamStatusRegistry,
                                        SubscriptionsRegistry subscriptionRegistry) {
        this.subscriptionServiceClient = subscriptionServiceClient;
        this.streamStatusRegistry = streamStatusRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    @Override
    public Mono<Void> registerUser(UUID userId) {
        return subscriptionServiceClient.getTrackedStreamers(userId)
                .flatMapMany(Flux::fromIterable)
                .doOnNext(streamer -> {

                    subscriptionRegistry.getOrCreateUserSubscriptions(userId)
                            .add(streamer.getEntityId());

                    subscriptionRegistry
                            .getOrCreateStreamerSubscriptions(streamer.getEntityId())
                            .add(userId);

                    streamStatusRegistry.getOrCreate(streamer.getEntityId());
                })
                .then();
    }
}
