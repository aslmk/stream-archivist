package com.aslmk.streamstatusservice.kafka;


import com.aslmk.common.dto.RecordingStatusEvent;
import com.aslmk.common.dto.StreamLifecycleEvent;
import com.aslmk.streamstatusservice.entity.RecordingStatus;
import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import com.aslmk.streamstatusservice.entity.StreamerKey;
import com.aslmk.streamstatusservice.service.StreamStatusPublisher;
import com.aslmk.streamstatusservice.service.impl.StreamStatusRegistry;
import com.aslmk.streamstatusservice.service.impl.StreamerIdentityResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaListeners {

    private final StreamStatusRegistry registry;
    private final StreamerIdentityResolver resolver;
    private final StreamStatusPublisher publisher;

    public KafkaListeners(StreamStatusRegistry registry,
                          StreamerIdentityResolver resolver,
                          StreamStatusPublisher publisher) {
        this.registry = registry;
        this.resolver = resolver;
        this.publisher = publisher;
    }

    @KafkaListener(topics = "${user.kafka.topic-stream-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleStreamLifecycle(@Payload StreamLifecycleEvent request) {
        StreamerKey streamerKey = new StreamerKey(request.getProviderUserId(), request.getProviderName());
        UUID streamerId = resolver.resolve(streamerKey);

        StreamStatusEntity streamStatus = registry.getOrCreate(streamerId);

        switch (request.getEventType()) {
            case STREAM_STARTED -> streamStatus.setLive(true);
            case STREAM_ENDED -> streamStatus.setLive(false);
        }

        publisher.publish(streamStatus);
    }

    @KafkaListener(topics = "${user.kafka.topic-recording-lifecycle-events}", groupId = "${user.kafka.group-id}")
    public void handleRecordingLifecycle(@Payload RecordingStatusEvent request) {
        StreamerKey streamerKey = new StreamerKey(request.getProviderUserId(), request.getProviderName());
        UUID streamerId = resolver.resolve(streamerKey);

        StreamStatusEntity streamStatus = registry.getOrCreate(streamerId);

        switch (request.getEventType()) {
            case RECORDING_STARTED -> streamStatus.setRecordingStatus(RecordingStatus.RECORDING);
            case RECORDING_FINISHED -> streamStatus.setRecordingStatus(RecordingStatus.FINISHED);
            case RECORDING_FAILED -> streamStatus.setRecordingStatus(RecordingStatus.FAILED);
        }

        publisher.publish(streamStatus);
    }
}

