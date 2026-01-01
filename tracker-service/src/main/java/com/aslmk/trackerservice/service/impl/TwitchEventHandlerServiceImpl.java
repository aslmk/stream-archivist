package com.aslmk.trackerservice.service.impl;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.StreamerNotFoundException;
import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.TwitchEventHandlerService;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class TwitchEventHandlerServiceImpl implements TwitchEventHandlerService {
    private final KafkaService kafkaService;
    private final StreamerService streamerService;

    public TwitchEventHandlerServiceImpl(KafkaService kafkaService, StreamerService streamerService) {
        this.kafkaService = kafkaService;
        this.streamerService = streamerService;
    }

    @Override
    public void handle(TwitchEventSubRequest request) {
        String eventType = request.getSubscription().getType();
        String login = request.getEvent().getBroadcaster_user_login();
        String id = request.getEvent().getBroadcaster_user_id();

        log.info("Processing Twitch event: type='{}', streamer='{}', streamerId='{}'", eventType, login, id);

        if ("stream.online".equals(eventType)) {
            log.info("Stream started: streamer='{}', streamerId='{}'", login, id);
            StreamerEntity streamer = getStreamer(id);
            streamerService.updateStatus(streamer, true);
        } else if ("stream.offline".equals(eventType)) {
            log.info("Stream ended: streamer='{}', streamerId='{}'", login, id);
            StreamerEntity streamer = getStreamer(id);
            streamerService.updateStatus(streamer, false);
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

    private StreamerEntity getStreamer(String id) {
        String providerName = "twitch";

        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(id, providerName);

        if (dbStreamer.isEmpty()) {
            log.error("Streamer not found: id='{}', provider='{}'", id, providerName);
            throw new StreamerNotFoundException(
                    String.format("Streamer not found: id='%s', provider='%s'", id, providerName)
            );
        }

        StreamerEntity streamer = dbStreamer.get();
        log.info("Found streamer with id='{}'", streamer.getId());
        return streamer;
    }
}
