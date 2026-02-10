package com.aslmk.streamstatusservice.kafka;


import com.aslmk.common.dto.RecordingStatusEvent;
import com.aslmk.common.dto.StreamLifecycleEvent;
import com.aslmk.streamstatusservice.entity.RecordingStatus;
import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import com.aslmk.streamstatusservice.service.StreamStatusPublisher;
import com.aslmk.streamstatusservice.service.impl.StreamStatusRegistry;
import com.aslmk.streamstatusservice.service.impl.StreamStatusSsePublisher;
import com.aslmk.streamstatusservice.service.impl.SubscriptionsRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class KafkaListeners {

    private final StreamStatusRegistry registry;
    private final StreamStatusPublisher publisher;
    private final SubscriptionsRegistry subscriptionsRegistry;

    public KafkaListeners(StreamStatusRegistry registry,
                          StreamStatusSsePublisher publisher, SubscriptionsRegistry subscriptionsRegistry) {
        this.registry = registry;
        this.publisher = publisher;
        this.subscriptionsRegistry = subscriptionsRegistry;
    }

    @KafkaListener(topics = "${user.kafka.topic-stream-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleStreamLifecycle(@Payload StreamLifecycleEvent request) {
        UUID streamerId = request.getStreamerId();

        StreamStatusEntity streamStatus = registry.getOrCreate(streamerId);

        switch (request.getEventType()) {
            case STREAM_STARTED -> streamStatus.setLive(true);
            case STREAM_ENDED -> streamStatus.setLive(false);
        }

        Set<UUID> userIds = subscriptionsRegistry.getOrCreateStreamerSubscriptions(streamerId);

        publisher.publish(streamStatus, streamerId, userIds);
    }

    @KafkaListener(topics = "${user.kafka.topic-recording-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleRecordingLifecycle(@Payload RecordingStatusEvent request) {
        UUID streamerId = request.getStreamerId();

        StreamStatusEntity streamStatus = registry.getOrCreate(streamerId);

        switch (request.getEventType()) {
            case RECORDING_STARTED -> streamStatus.setRecordingStatus(RecordingStatus.RECORDING);
            case RECORDING_FINISHED -> streamStatus.setRecordingStatus(RecordingStatus.FINISHED);
            case RECORDING_FAILED -> streamStatus.setRecordingStatus(RecordingStatus.FAILED);
        }

        Set<UUID> userIds = subscriptionsRegistry.getOrCreateStreamerSubscriptions(streamerId);

        publisher.publish(streamStatus, streamerId, userIds);
    }
}

