package com.aslmk.streamstatusservice.kafka;


import com.aslmk.streamstatusservice.dto.RecordingStatusEvent;
import com.aslmk.streamstatusservice.dto.StreamLifecycleEvent;
import com.aslmk.streamstatusservice.domain.RecordingStatus;
import com.aslmk.streamstatusservice.domain.StreamState;
import com.aslmk.streamstatusservice.exception.KafkaEventDeserializationException;
import com.aslmk.streamstatusservice.service.StreamStatusPublisher;
import com.aslmk.streamstatusservice.registry.StreamStatusRegistry;
import com.aslmk.streamstatusservice.service.StreamStatusSsePublisher;
import com.aslmk.streamstatusservice.registry.SubscriptionsRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.UUID;

@Component
public class KafkaListeners {

    private final StreamStatusRegistry registry;
    private final StreamStatusPublisher publisher;
    private final SubscriptionsRegistry subscriptionsRegistry;
    private final ObjectMapper objectMapper;

    public KafkaListeners(StreamStatusRegistry registry,
                          StreamStatusSsePublisher publisher, SubscriptionsRegistry subscriptionsRegistry,
                          ObjectMapper objectMapper) {
        this.registry = registry;
        this.publisher = publisher;
        this.subscriptionsRegistry = subscriptionsRegistry;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${user.kafka.topic-stream-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleStreamLifecycle(@Payload String payload) {
        StreamLifecycleEvent event = deserialize(payload, StreamLifecycleEvent.class);

        UUID streamerId = event.getStreamerId();

        StreamState streamState = registry.getOrCreate(streamerId);

        switch (event.getEventType()) {
            case STREAM_STARTED -> streamState.setLive(true);
            case STREAM_ENDED -> streamState.setLive(false);
        }

        Set<UUID> userIds = subscriptionsRegistry.getOrCreateStreamerSubscriptions(streamerId);

        publisher.publish(streamState, streamerId, userIds);
    }

    @KafkaListener(topics = "${user.kafka.topic-recording-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleRecordingLifecycle(@Payload String payload) {
        RecordingStatusEvent event = deserialize(payload, RecordingStatusEvent.class);

        UUID streamerId = event.getStreamerId();

        StreamState streamState = registry.getOrCreate(streamerId);

        switch (event.getEventType()) {
            case RECORDING_STARTED -> streamState.setRecordingStatus(RecordingStatus.RECORDING);
            case RECORDING_FINISHED -> streamState.setRecordingStatus(RecordingStatus.FINISHED);
            case RECORDING_FAILED -> streamState.setRecordingStatus(RecordingStatus.FAILED);
        }

        Set<UUID> userIds = subscriptionsRegistry.getOrCreateStreamerSubscriptions(streamerId);

        publisher.publish(streamState, streamerId, userIds);
    }

    private <T> T deserialize(String data, Class<T> c) {
        try {
            return objectMapper.readValue(data, c);
        } catch (JacksonException e) {
            throw new KafkaEventDeserializationException(
                    String.format("Failed to deserialize JSON as instance of class '%s'", c.getSimpleName()), e);
        }
    }
}

