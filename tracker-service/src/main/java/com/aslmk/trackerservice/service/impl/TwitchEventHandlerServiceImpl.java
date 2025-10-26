package com.aslmk.trackerservice.service.impl;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.service.TwitchEventHandlerService;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwitchEventHandlerServiceImpl implements TwitchEventHandlerService {
    private final KafkaService kafkaService;

    public TwitchEventHandlerServiceImpl(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Override
    public void handle(TwitchEventSubRequest request) {
        String eventType = request.getSubscription().getType();
        String login = request.getEvent().getBroadcaster_user_login();
        String id = request.getEvent().getBroadcaster_user_id();

        if ("stream.online".equals(eventType)) {
            log.info("🔥 Stream started: {} ({})", login, id);
        } else if ("stream.offline".equals(eventType)) {
            log.info("❌ Stream ended: {} ({})", login, id);
            return;
        } else {
            log.info("📦 Unknown event type: {}", eventType);
            throw new UnknownEventTypeException("Unknown event type: " + eventType);
        }

        String streamUrl = "https://twitch.tv/" + login;

        RecordingRequestDto recordingRequestDto = RecordingRequestDto.builder()
                .streamerUsername(login)
                .streamUrl(streamUrl)
                .streamQuality("480p")
                .build();

        kafkaService.send(recordingRequestDto);
    }
}
