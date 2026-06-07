package com.aslmk.trackerservice.service.event;

import com.aslmk.trackerservice.client.twitch.dto.TwitchEventSubRequest;
import com.aslmk.trackerservice.domain.EventType;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.dto.StreamLifecycleType;
import com.aslmk.trackerservice.service.streamer.StreamerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@Transactional
public class TwitchEventHandlerServiceImpl implements TwitchEventHandlerService {
    private final StreamerService streamerService;
    private final EventProcessedService eventService;
    private final EventLogService eventLogService;

    private static final String PROVIDER_NAME = "twitch";
    private static final String TWITCH_BASE_URL = "https://twitch.tv/";

    public TwitchEventHandlerServiceImpl(StreamerService streamerService,
                                         EventProcessedService eventService,
                                         EventLogService eventLogService) {
        this.streamerService = streamerService;
        this.eventService = eventService;
        this.eventLogService = eventLogService;
    }

    @Override
    public void handle(TwitchEventSubRequest request, String eventId) {
        if (!eventService.tryMarkAsProcessed(eventId)) {
            log.debug("Duplicate event ignored",
                    kv("eventId", eventId));
            return;
        }

        String eventType = request.getSubscription().getType();
        String login = request.getEvent().getBroadcaster_user_login();
        String id = request.getEvent().getBroadcaster_user_id();

        log.debug("Processing Twitch event",
                kv("eventType", eventType),
                kv("streamerUsername", login),
                kv("providerUserId", id));

        StreamLifecycleType streamType = StreamLifecycleType.fromValue(eventType);
        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(id, PROVIDER_NAME);

        if (dbStreamer.isEmpty()) {
            log.warn("Streamer not found in the database",
                    kv("providerStreamerId", id),
                    kv("providerName", PROVIDER_NAME));
            return;
        }
        StreamerEntity streamer = dbStreamer.get();

        StreamLifecycleEvent dto = StreamLifecycleEvent.builder()
                .eventId(UUID.randomUUID())
                .streamerUsername(login)
                .streamUrl(getStreamUrl(login))
                .streamerId(streamer.getId())
                .eventType(streamType)
                .build();

        eventLogService.save(dto, EventType.fromString(streamType.name()));

        log.info("Processed Twitch event",
                kv("eventType", eventType),
                kv("streamerUsername", login),
                kv("providerUserId", id));
    }

    private String getStreamUrl(String username) {
        return TWITCH_BASE_URL + username;
    }
}
