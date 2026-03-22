package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.*;
import com.aslmk.trackerservice.entity.StreamTrackingSubscriptionEntity;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.service.StreamTrackingSubscriptionService;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.TrackingService;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClient;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchWebhookSubscriptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final TwitchApiClient twitchClient;
    private final StreamerService streamerService;
    private final StreamTrackingSubscriptionService trackingSubscriptionService;

    private static final String EVENT_TYPE_ONLINE = "stream.online";
    private static final String EVENT_TYPE_OFFLINE = "stream.offline";

    public TrackingServiceImpl(TwitchApiClient twitchClient,
                               StreamerService streamerService,
                               StreamTrackingSubscriptionService trackingSubscriptionService) {
        this.twitchClient = twitchClient;
        this.streamerService = streamerService;
        this.trackingSubscriptionService = trackingSubscriptionService;
    }

    @Override
    public TrackStreamerResponse trackStreamer(TrackingRequestDto trackingRequest) {
        validateTrackingRequest(trackingRequest);

        String streamerUsername = trackingRequest.getStreamerUsername();
        String streamerProviderName = trackingRequest.getProviderName();

        Optional<TrackStreamerResponse> trackedStreamer = checkIfTracked(streamerUsername);
        if (trackedStreamer.isPresent()) return trackedStreamer.get();

        log.debug("Fetching streamer info from Twitch API for username='{}'", streamerUsername);
        TwitchStreamerInfo streamerInfo = twitchClient.getStreamerInfo(streamerUsername);
        String streamerTwitchId = streamerInfo.getId();

        Optional<TrackStreamerResponse> streamerWithUpdatedUsername = checkIfStreamerUsernameUpdateRequired(streamerTwitchId,
                streamerProviderName, streamerUsername);
        if (streamerWithUpdatedUsername.isPresent()) return streamerWithUpdatedUsername.get();

        log.info("Creating new streamer entry in DB: username='{}', streamerId='{}', provider='{}'",
                streamerUsername, streamerTwitchId, streamerProviderName);

        StreamerEntity createdStreamer = createStreamer(streamerUsername, streamerInfo, streamerProviderName);

        try {
            createWebhookSubscription(streamerTwitchId, EVENT_TYPE_ONLINE, createdStreamer.getId());
            createWebhookSubscription(streamerTwitchId, EVENT_TYPE_OFFLINE, createdStreamer.getId());
        } catch (TwitchApiClientException e) {
            log.error("Failed to create Twitch webhook subscriptions for streamerId={}. Rolling back streamer creation.",
                    createdStreamer.getId(), e);
            throw e;
        }


        log.info("Tracking setup completed: streamer {}, provider {}", streamerUsername, streamerProviderName);

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
    }

    private void validateTrackingRequest(TrackingRequestDto trackingRequest) {
        if (trackingRequest == null) {
            log.warn("Tracking request validation failed: request is null");
            throw new TrackingException("Tracking request is null");
        }

        if (trackingRequest.getStreamerUsername() == null || trackingRequest.getStreamerUsername().isBlank()) {
            log.warn("Tracking request validation failed: streamer username is null or blank");
            throw new TrackingException("Streamer username cannot be null or blank");
        }

        if (trackingRequest.getProviderName() == null || trackingRequest.getProviderName().isBlank()) {
            log.warn("Tracking request validation failed: provider name is null or blank");
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

        log.debug("Saving to DB: streamer={}, streamerId={}, provider={}", username, streamerInfo.getId(), providerName);
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
            log.info("Streamer='{}' already tracked — skipping subscription", streamer.getUsername());
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
            log.info("Streamer with id='{}' already exists in DB but username differs. Updating username to '{}'",
                    streamerTwitchId, newUsername);
            streamerService.updateUsername(streamer, newUsername);
            TrackStreamerResponse response = mapper(streamer);
            return Optional.of(response);
        }

        return Optional.empty();
    }

    private void createWebhookSubscription(String streamerTwitchId, String eventType, UUID streamerInternalId) {
        log.info("Creating webhook subscription ('{}') for streamerId='{}'",eventType, streamerTwitchId);
        TwitchWebhookSubscriptionResponse response = twitchClient
                .subscribeToStreamer(streamerTwitchId, eventType);

        CreateTrackingSubscriptionDto dto = CreateTrackingSubscriptionDto.builder()
                .subscriptionId(response.getId())
                .subscriptionType(response.getType())
                .providerName("twitch")
                .streamerInternalId(streamerInternalId)
                .build();

        trackingSubscriptionService.saveSubscription(dto);
    }
}
