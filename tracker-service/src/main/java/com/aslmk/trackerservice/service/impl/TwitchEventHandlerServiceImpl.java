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

        log.info("Processing Twitch event: type='{}', streamer='{}', streamerId='{}'", eventType, login, id);

        if ("stream.online".equals(eventType)) {
            log.info("Stream started: streamer='{}', streamerId='{}'", login, id);
        } else if ("stream.offline".equals(eventType)) {
            log.info("Stream ended: streamer='{}', streamerId='{}'", login, id);
            return;
        } else {
            log.error("Received unsupported Twitch event type='{}'", eventType);
            throw new UnknownEventTypeException("Unknown event type: " + eventType);
        }

        String streamUrl = "https://twitch.tv/" + login;

        RecordingRequestDto dto = RecordingRequestDto.builder()
                .streamerUsername(login)
                .streamUrl(streamUrl)
                .streamQuality("480p")
                .build();

        log.debug("Sending RecordingRequest to Kafka: streamer='{}', streamUrl='{}', streamQuality='{}'",
                dto.getStreamerUsername(), dto.getStreamUrl(), dto.getStreamQuality());

        kafkaService.send(dto);
        log.info("Kafka message sent successfully for streamer='{}'", login);
    }
}
