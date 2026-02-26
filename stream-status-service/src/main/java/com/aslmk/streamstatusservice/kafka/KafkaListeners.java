package com.aslmk.streamstatusservice.kafka;


import com.aslmk.streamstatusservice.dto.RecordingStatusEvent;
import com.aslmk.streamstatusservice.dto.StreamLifecycleEvent;
import com.aslmk.streamstatusservice.entity.RecordingStatus;
import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import com.aslmk.streamstatusservice.exception.KafkaEventDeserializationException;
import com.aslmk.streamstatusservice.service.StreamStatusPublisher;
import com.aslmk.streamstatusservice.service.impl.StreamStatusRegistry;
import com.aslmk.streamstatusservice.service.impl.StreamStatusSsePublisher;
import com.aslmk.streamstatusservice.service.impl.SubscriptionsRegistry;
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

        StreamStatusEntity streamStatus = registry.getOrCreate(streamerId);

        switch (event.getEventType()) {
            case STREAM_STARTED -> streamStatus.setLive(true);
            case STREAM_ENDED -> streamStatus.setLive(false);
        }

        Set<UUID> userIds = subscriptionsRegistry.getOrCreateStreamerSubscriptions(streamerId);

        publisher.publish(streamStatus, streamerId, userIds);
    }

    @KafkaListener(topics = "${user.kafka.topic-recording-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleRecordingLifecycle(@Payload String payload) {
        RecordingStatusEvent event = deserialize(payload, RecordingStatusEvent.class);

        UUID streamerId = event.getStreamerId();

        StreamStatusEntity streamStatus = registry.getOrCreate(streamerId);

        switch (event.getEventType()) {
            case RECORDING_STARTED -> streamStatus.setRecordingStatus(RecordingStatus.RECORDING);
            case RECORDING_FINISHED -> streamStatus.setRecordingStatus(RecordingStatus.FINISHED);
            case RECORDING_FAILED -> streamStatus.setRecordingStatus(RecordingStatus.FAILED);
        }

        Set<UUID> userIds = subscriptionsRegistry.getOrCreateStreamerSubscriptions(streamerId);

        publisher.publish(streamStatus, streamerId, userIds);
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

