package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.dto.TrackStreamerResponse;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.dto.WebhookSubscriptionDto;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.service.streamer.StreamerService;
import com.aslmk.trackerservice.service.subscription.StreamTrackingSubscriptionService;
import com.aslmk.trackerservice.service.subscription.TrackingServiceImpl;
import com.aslmk.trackerservice.service.subscription.WebhookSubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;

@ExtendWith(MockitoExtension.class)
class TrackingServiceUnitTests {

    @Mock
    private TwitchApiClient twitchClient;

    @Mock
    private StreamerService streamerService;

    @Mock
    private StreamTrackingSubscriptionService trackingSubscriptionService;

    @Mock
    private WebhookSubscriptionService webhookSubscriptionService;

    @InjectMocks
    private TrackingServiceImpl trackingService;

    private TrackingRequestDto validRequest;

    private static final String STREAMER_USERNAME = "test0";
    private static final String PROVIDER_NAME = "twitch";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROFILE_IMAGE_URL = "image-url";
    private static final String EVENT_TYPE_ONLINE = "stream.online";
    private static final String EVENT_TYPE_OFFLINE = "stream.offline";

    private static TwitchStreamerInfo streamerInfo;
    private static StreamerEntity validStreamerEntity;

    @BeforeEach
    void setUp() {
        validRequest = TrackingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .build();

        streamerInfo = TwitchStreamerInfo.builder()
                .id(PROVIDER_USER_ID)
                .profileImageUrl(PROFILE_IMAGE_URL)
                .build();

        validStreamerEntity = StreamerEntity.builder()
                .id(UUID.randomUUID())
                .profileImageUrl(PROFILE_IMAGE_URL)
                .providerUserId(PROVIDER_USER_ID)
                .providerName(PROVIDER_NAME)
                .username(STREAMER_USERNAME)
                .build();
    }

    @Test
    void should_throwTrackingException_when_requestIsNull() {
        Assertions.assertThrows(TrackingException.class,
                () -> trackingService.trackStreamer(null));
    }

    @Test
    void should_throwTrackingException_when_usernameIsBlank() {
        validRequest.setStreamerUsername(" ");
        Assertions.assertThrows(TrackingException.class,
                () -> trackingService.trackStreamer(validRequest));
    }

    @Test
    void should_throwTrackingException_when_providerNameIsBlank() {
        validRequest.setProviderName(" ");
        Assertions.assertThrows(TrackingException.class,
                () -> trackingService.trackStreamer(validRequest));
    }

    @Test
    void should_returnImmediately_when_streamerAlreadyTracked() {
        Mockito.when(streamerService.findByUsername(STREAMER_USERNAME))
                .thenReturn(Optional.of(validStreamerEntity));

        TrackStreamerResponse response = trackingService.trackStreamer(validRequest);

        Assertions.assertEquals(STREAMER_USERNAME, response.getStreamerUsername());
        Mockito.verify(streamerService).findByUsername(STREAMER_USERNAME);
        Mockito.verifyNoMoreInteractions(streamerService, twitchClient, webhookSubscriptionService);
    }

    @Test
    void should_updateUsername_when_streamerExistsByIdAndProvider() {
        Mockito.when(streamerService.findByUsername(STREAMER_USERNAME)).thenReturn(Optional.empty());
        Mockito.when(twitchClient.getStreamerInfo(STREAMER_USERNAME)).thenReturn(streamerInfo);
        Mockito.when(streamerService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME))
                .thenReturn(Optional.of(validStreamerEntity));

        TrackStreamerResponse response = trackingService.trackStreamer(validRequest);

        Assertions.assertEquals(STREAMER_USERNAME, response.getStreamerUsername());
        Mockito.verify(streamerService)
                .updateUsername(Mockito.any(StreamerEntity.class), Mockito.eq(STREAMER_USERNAME));
        Mockito.verify(streamerService, Mockito.never()).create(any());
        Mockito.verify(webhookSubscriptionService, Mockito.never()).saveSubscription(any());
    }

    @Test
    void should_createStreamerAndSavePendingSubscriptions_when_notTrackedAndNotInDb() {
        Mockito.when(streamerService.findByUsername(STREAMER_USERNAME)).thenReturn(Optional.empty());
        Mockito.when(twitchClient.getStreamerInfo(STREAMER_USERNAME)).thenReturn(streamerInfo);
        Mockito.when(streamerService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME))
                .thenReturn(Optional.empty());
        Mockito.when(streamerService.create(Mockito.any())).thenReturn(validStreamerEntity);

        TrackStreamerResponse response = trackingService.trackStreamer(validRequest);

        Assertions.assertEquals(STREAMER_USERNAME, response.getStreamerUsername());
        Mockito.verify(streamerService).create(argThat(dto ->
                dto.getUsername().equals(STREAMER_USERNAME) &&
                        dto.getStreamerId().equals(PROVIDER_USER_ID) &&
                        dto.getProviderName().equals(PROVIDER_NAME)));

        Mockito.verify(twitchClient, Mockito.never()).subscribeToStreamer(Mockito.anyString(), Mockito.anyString());

        ArgumentCaptor<WebhookSubscriptionDto> captor = ArgumentCaptor.forClass(WebhookSubscriptionDto.class);
        Mockito.verify(webhookSubscriptionService, Mockito.times(2))
                .saveSubscription(captor.capture());

        List<String> subscriptionTypes = captor.getAllValues().stream()
                .map(WebhookSubscriptionDto::getSubscriptionType)
                .toList();

        Assertions.assertTrue(subscriptionTypes.contains(EVENT_TYPE_ONLINE));
        Assertions.assertTrue(subscriptionTypes.contains(EVENT_TYPE_OFFLINE));

        captor.getAllValues().forEach(sub -> {
            Assertions.assertEquals(WebhookSubscriptionStatus.PENDING.name(), sub.getSubscriptionStatus());
            Assertions.assertEquals(validStreamerEntity.getId(), sub.getStreamerInternalId());
            Assertions.assertEquals(PROVIDER_USER_ID, sub.getStreamerProviderId());
            Assertions.assertEquals(PROVIDER_NAME, sub.getProviderName());
            Assertions.assertNull(sub.getSubscriptionId());
            Assertions.assertEquals(0, sub.getRetryCount());
        });
    }
}