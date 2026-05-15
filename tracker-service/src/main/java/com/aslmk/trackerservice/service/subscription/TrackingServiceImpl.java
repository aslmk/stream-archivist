package com.aslmk.trackerservice.service.subscription;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.domain.StreamTrackingSubscriptionEntity;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.dto.*;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.service.streamer.StreamerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final TwitchApiClient twitchClient;
    private final StreamerService streamerService;
    private final StreamTrackingSubscriptionService trackingSubscriptionService;
    private final WebhookSubscriptionService webhookSubscriptionService;

    private static final String EVENT_TYPE_ONLINE = "stream.online";
    private static final String EVENT_TYPE_OFFLINE = "stream.offline";

    public TrackingServiceImpl(TwitchApiClient twitchClient,
                               StreamerService streamerService,
                               StreamTrackingSubscriptionService trackingSubscriptionService,
                               WebhookSubscriptionService webhookSubscriptionService) {
        this.twitchClient = twitchClient;
        this.streamerService = streamerService;
        this.trackingSubscriptionService = trackingSubscriptionService;
        this.webhookSubscriptionService = webhookSubscriptionService;
    }

    @Override
    public TrackStreamerResponse trackStreamer(TrackingRequestDto trackingRequest) {
        validateTrackingRequest(trackingRequest);

        String streamerUsername = trackingRequest.getStreamerUsername();
        String providerName = trackingRequest.getProviderName();

        Optional<TrackStreamerResponse> trackedStreamer = checkIfTracked(streamerUsername);
        if (trackedStreamer.isPresent()) return trackedStreamer.get();

        log.debug("Fetching streamer info from Twitch API",
                kv("streamerUsername", streamerUsername));
        TwitchStreamerInfo streamerInfo = twitchClient.getStreamerInfo(streamerUsername);
        String streamerTwitchId = streamerInfo.getId();

        Optional<TrackStreamerResponse> streamerWithUpdatedUsername = checkIfStreamerUsernameUpdateRequired(
                streamerTwitchId, providerName, streamerUsername);

        if (streamerWithUpdatedUsername.isPresent()) return streamerWithUpdatedUsername.get();

        StreamerEntity createdStreamer = createStreamer(streamerUsername,
                streamerInfo, providerName);

        createWebhookSubscription(EVENT_TYPE_ONLINE, createdStreamer);
        createWebhookSubscription(EVENT_TYPE_OFFLINE, createdStreamer);

        log.info("Tracking setup completed",
                kv("streamerUsername", streamerUsername),
                kv("providerName", providerName));

        return mapper(createdStreamer);
    }

    @Override
    public void unsubscribe(String streamerId) {
        UUID uuidStreamerId = UUID.fromString(streamerId);

        List<StreamTrackingSubscriptionEntity> subscriptions = trackingSubscriptionService
                .getAllSubscriptionsByStreamerId(uuidStreamerId);

        subscriptions.forEach(sub -> {
            twitchClient.unsubscribeFromStreamer(sub.getSubscriptionId(), sub.getSubscriptionType());
            trackingSubscriptionService
                    .deleteSubscription(DeleteTrackingSubscriptionDto.builder()
                            .subscriptionId(sub.getSubscriptionId())
                            .build());
            streamerService.deleteById(uuidStreamerId);
        });

        log.debug("Unsubscribed and deleted subscription",
                kv("streamerId", uuidStreamerId));
    }

    private void validateTrackingRequest(TrackingRequestDto trackingRequest) {
        if (trackingRequest == null) {
            throw new TrackingException("Tracking request is null");
        }

        if (trackingRequest.getStreamerUsername() == null || trackingRequest.getStreamerUsername().isBlank()) {
            throw new TrackingException("Streamer username cannot be null or blank");
        }

        if (trackingRequest.getProviderName() == null || trackingRequest.getProviderName().isBlank()) {
            throw new TrackingException("Provider name cannot be null or blank");
        }
    }

    private StreamerEntity createStreamer(String username,
                                          TwitchStreamerInfo streamerInfo,
                                          String providerName) {
        CreateStreamerDto dto = CreateStreamerDto.builder()
                .username(username)
                .streamerId(streamerInfo.getId())
                .profileImageUrl(streamerInfo.getProfileImageUrl())
                .providerName(providerName)
                .build();

        return streamerService.create(dto);
    }

    private TrackStreamerResponse mapper(StreamerEntity entity) {
        return TrackStreamerResponse.builder()
                .streamerId(entity.getId())
                .streamerUsername(entity.getUsername())
                .streamerProfileImageUrl(entity.getProfileImageUrl())
                .providerName(entity.getProviderName())
                .build();
    }

    private Optional<TrackStreamerResponse> checkIfTracked(String username) {
        Optional<StreamerEntity> trackedStreamer = streamerService
                .findByUsername(username);

        if (trackedStreamer.isPresent()) {
            StreamerEntity streamer = trackedStreamer.get();
            log.debug("Streamer is already tracked",
                    kv("streamerUsername", streamer.getUsername()),
                    kv("streamerId", streamer.getId()));
            TrackStreamerResponse response = mapper(streamer);
            return Optional.of(response);
        }
        return Optional.empty();
    }

    private Optional<TrackStreamerResponse> checkIfStreamerUsernameUpdateRequired(String streamerTwitchId,
                                                                                  String providerName,
                                                                                  String newUsername) {
        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(streamerTwitchId, providerName);

        if (dbStreamer.isPresent()) {
            StreamerEntity streamer = dbStreamer.get();
            streamerService.updateUsername(streamer, newUsername);
            TrackStreamerResponse response = mapper(streamer);
            log.debug("Streamer is already exists in DB with different username; updated DB username",
                    kv("providerUserId", streamerTwitchId),
                    kv("oldUsername", streamer.getUsername()),
                    kv("newUsername", newUsername));
            return Optional.of(response);
        }

        return Optional.empty();
    }

    private void createWebhookSubscription(String eventType, StreamerEntity streamer) {
        WebhookSubscriptionDto dto = WebhookSubscriptionDto.builder()
                .streamerInternalId(streamer.getId())
                .streamerProviderId(streamer.getProviderUserId())
                .providerName(streamer.getProviderName())
                .retryCount(0)
                .subscriptionType(eventType)
                .subscriptionStatus(WebhookSubscriptionStatus.PENDING.name())
                .subscriptionId(null)
                .build();

        webhookSubscriptionService.saveSubscription(dto);

        log.debug("Webhook subscription saved",
                kv("subscriptionType", eventType),
                kv("streamerId", streamer.getId()));
    }
}
