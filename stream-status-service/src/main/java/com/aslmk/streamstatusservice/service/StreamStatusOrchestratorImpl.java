package com.aslmk.streamstatusservice.service;

import com.aslmk.streamstatusservice.client.SubscriptionServiceClient;
import com.aslmk.streamstatusservice.registry.StreamStatusRegistry;
import com.aslmk.streamstatusservice.registry.SubscriptionsRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
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
                            .add(streamer.getId());

                    subscriptionRegistry
                            .getOrCreateStreamerSubscriptions(streamer.getId())
                            .add(userId);

                    streamStatusRegistry.getOrCreate(streamer.getId());
                })
                .then()
                .doOnSuccess(unused -> log.debug("SSE user registered", kv("userId", userId)))
                .doOnError(error -> log.error("Failed to register SSE user", kv("userId", userId), error));
    }
}
