package com.aslmk.streamstatusservice.service;

import com.aslmk.streamstatusservice.domain.StreamState;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamStatusSsePublisher implements StreamStatusPublisher {

    private final Map<UUID, Sinks.Many<StreamState>> userSinks = new ConcurrentHashMap<>();


    public Flux<StreamState> register(UUID userId) {
        Sinks.Many<StreamState> sink = Sinks.many().unicast().onBackpressureBuffer();
        userSinks.put(userId, sink);
        return sink.asFlux();
    }


    @Override
    public void publish(StreamState state, UUID streamerId, Set<UUID> userIds) {
        for (UUID userId : userIds) {
            Sinks.Many<StreamState> sink = userSinks.get(userId);
            if (sink != null) {
                sink.tryEmitNext(state);
            }
        }
    }

    public void unregister(UUID userId) {
        userSinks.remove(userId);
    }
}
