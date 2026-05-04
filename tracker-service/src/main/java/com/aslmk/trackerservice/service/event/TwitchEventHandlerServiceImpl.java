package com.aslmk.trackerservice.service.event;

import com.aslmk.trackerservice.dto.StreamLifecycleType;
import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.exception.StreamerNotFoundException;
import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.service.streamer.StreamerService;
import com.aslmk.trackerservice.client.twitch.dto.TwitchEventSubRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TwitchEventHandlerServiceImpl implements TwitchEventHandlerService {
    private final KafkaService kafkaService;
    private final StreamerService streamerService;
    private final EventProcessedService eventService;

    private static final String PROVIDER_NAME = "twitch";

    public TwitchEventHandlerServiceImpl(KafkaService kafkaService,
                                         StreamerService streamerService,
                                         EventProcessedService eventService) {
        this.kafkaService = kafkaService;
        this.streamerService = streamerService;
        this.eventService = eventService;
    }

    @Override
    public void handle(TwitchEventSubRequest request, String eventId) {
        if (!eventService.tryMarkAsProcessed(eventId)) {
            log.info("Duplicate event ignored: eventId='{}'", eventId);
            return;
        }

        String eventType = request.getSubscription().getType();
        String login = request.getEvent().getBroadcaster_user_login();
        String id = request.getEvent().getBroadcaster_user_id();

        log.info("Processing Twitch event: type='{}', streamer='{}', streamerId='{}'", eventType, login, id);

        StreamLifecycleType streamLifecycleType;
        StreamerEntity streamer = getStreamer(id);

        if ("stream.online".equals(eventType)) {
            log.info("Stream started: streamer='{}', streamerId='{}'", login, id);
            streamLifecycleType = StreamLifecycleType.STREAM_STARTED;
        } else if ("stream.offline".equals(eventType)) {
            log.info("Stream ended: streamer='{}', streamerId='{}'", login, id);
            streamLifecycleType = StreamLifecycleType.STREAM_ENDED;
        } else {
            log.error("Received unsupported Twitch event type='{}'", eventType);
            throw new UnknownEventTypeException("Unknown event type: " + eventType);
        }

        String streamUrl = "https://twitch.tv/" + login;

        StreamLifecycleEvent dto = StreamLifecycleEvent.builder()
                .streamerUsername(login)
                .streamUrl(streamUrl)
                .streamerId(streamer.getId())
                .eventType(streamLifecycleType)
                .build();

        log.debug("Sending StreamLifecycleEvent to Kafka: streamer='{}', streamUrl='{}'",
                dto.getStreamerUsername(), dto.getStreamUrl());

        kafkaService.send(dto);
        log.info("Kafka message sent successfully for streamer='{}'", login);
    }

    private StreamerEntity getStreamer(String id) {
        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(id, PROVIDER_NAME);

        if (dbStreamer.isEmpty()) {
            log.error("Streamer not found: id='{}', provider='{}'", id, PROVIDER_NAME);
            throw new StreamerNotFoundException(
                    String.format("Streamer not found: id='%s', provider='%s'", id, PROVIDER_NAME)
            );
        }

        StreamerEntity streamer = dbStreamer.get();
        log.info("Found streamer with id='{}'", streamer.getId());
        return streamer;
    }
}
