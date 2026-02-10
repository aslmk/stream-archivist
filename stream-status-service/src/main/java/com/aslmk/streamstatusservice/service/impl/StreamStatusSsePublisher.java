package com.aslmk.streamstatusservice.service.impl;

import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import com.aslmk.streamstatusservice.service.StreamStatusPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamStatusSsePublisher implements StreamStatusPublisher {

    private final Map<UUID, Sinks.Many<StreamStatusEntity>> userSinks = new ConcurrentHashMap<>();


    public Flux<StreamStatusEntity> register(UUID userId) {
        Sinks.Many<StreamStatusEntity> sink = Sinks.many().unicast().onBackpressureBuffer();
        userSinks.put(userId, sink);
        return sink.asFlux();
    }


    @Override
    public void publish(StreamStatusEntity status, UUID streamerId, Set<UUID> userIds) {
        for (UUID userId : userIds) {
            Sinks.Many<StreamStatusEntity> sink = userSinks.get(userId);
            if (sink != null) {
                sink.tryEmitNext(status);
            }
        }
    }

    public void unregister(UUID userId) {
        userSinks.remove(userId);
    }
}
