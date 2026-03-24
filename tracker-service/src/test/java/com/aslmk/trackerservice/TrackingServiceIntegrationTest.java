package com.aslmk.trackerservice;

import com.aslmk.trackerservice.dto.TrackStreamerResponse;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.domain.StreamTrackingSubscriptionEntity;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.repository.StreamTrackingSubscriptionRepository;
import com.aslmk.trackerservice.repository.StreamerRepository;
import com.aslmk.trackerservice.service.subscription.StreamTrackingSubscriptionService;
import com.aslmk.trackerservice.service.subscription.TrackingServiceImpl;
import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchStreamerInfo;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class TrackingServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test-db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private TrackingServiceImpl trackingService;

    @Autowired
    private StreamerRepository streamerRepository;

    @MockitoBean
    private TwitchApiClient twitchApiClient;

    @MockitoBean
    private StreamTrackingSubscriptionService trackingSubscriptionService;

    @MockitoBean
    private StreamTrackingSubscriptionRepository subscriptionRepository;

    private static final String STREAMER_USERNAME = "test0";
    private static final String PROVIDER_NAME = "twitch";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROFILE_IMAGE_URL = "image-url";

    private static TwitchStreamerInfo streamerInfo;

    @BeforeEach
    void setUp() {
        streamerInfo = TwitchStreamerInfo.builder()
                .id(PROVIDER_USER_ID)
                .profileImageUrl(PROFILE_IMAGE_URL)
                .build();
    }

    @Test
    void trackStreamer_should_doNothing_whenStreamerAlreadyTracked() {
        StreamerEntity existing = StreamerEntity.builder()
                .username(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .providerUserId(PROVIDER_USER_ID)
                .profileImageUrl(PROFILE_IMAGE_URL)
                .build();
        streamerRepository.save(existing);

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .build();

        TrackStreamerResponse response = trackingService.trackStreamer(dto);

        Assertions.assertEquals(1, streamerRepository.count());
        Assertions.assertEquals(STREAMER_USERNAME, response.getStreamerUsername());
        Mockito.verify(twitchApiClient, Mockito.never()).getStreamerInfo(Mockito.any());
        Mockito.verify(twitchApiClient, Mockito.never()).subscribeToStreamer(Mockito.any(), Mockito.anyString());
    }

    @Test
    void trackStreamer_should_updateUsername_whenStreamerExistsWithDifferentUsername() {
        StreamerEntity existing = StreamerEntity.builder()
                .username("old_test0")
                .providerName(PROVIDER_NAME)
                .providerUserId(PROVIDER_USER_ID)
                .profileImageUrl(PROFILE_IMAGE_URL)
                .build();
        streamerRepository.save(existing);

        Mockito.when(twitchApiClient.getStreamerInfo(STREAMER_USERNAME)).thenReturn(streamerInfo);

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .build();

        TrackStreamerResponse response = trackingService.trackStreamer(dto);

        Optional<StreamerEntity> updated = streamerRepository
                .findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME);

        Assertions.assertTrue(updated.isPresent());
        Assertions.assertEquals(STREAMER_USERNAME, updated.get().getUsername());
        Assertions.assertEquals(STREAMER_USERNAME, response.getStreamerUsername());
        Mockito.verify(twitchApiClient, Mockito.never()).subscribeToStreamer(Mockito.any(), Mockito.anyString());
    }

    @Test
    void trackStreamer_should_createAndSubscribe_whenStreamerCompletelyNew() {
        TwitchWebhookSubscriptionResponse onlineSubResponse = TwitchWebhookSubscriptionResponse.builder()
                .id(UUID.randomUUID())
                .type("stream.online")
                .build();
        TwitchWebhookSubscriptionResponse offlineSubResponse = TwitchWebhookSubscriptionResponse.builder()
                .id(UUID.randomUUID())
                .type("stream.offline")
                .build();

        Mockito.when(twitchApiClient.getStreamerInfo("test1"))
                .thenReturn(TwitchStreamerInfo.builder()
                        .id("999")
                        .profileImageUrl(PROFILE_IMAGE_URL)
                        .build());
        Mockito.when(twitchApiClient.subscribeToStreamer("999", "stream.online"))
                .thenReturn(onlineSubResponse);
        Mockito.when(twitchApiClient.subscribeToStreamer("999", "stream.offline"))
                .thenReturn(offlineSubResponse);

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername("test1")
                .providerName(PROVIDER_NAME)
                .build();

        TrackStreamerResponse response = trackingService.trackStreamer(dto);

        StreamerEntity created = streamerRepository
                .findByProviderUserIdAndProviderName("999", PROVIDER_NAME).orElseThrow();

        Assertions.assertEquals("test1", created.getUsername());
        Assertions.assertEquals("test1", response.getStreamerUsername());
        Mockito.verify(twitchApiClient).subscribeToStreamer("999", "stream.online");
        Mockito.verify(twitchApiClient).subscribeToStreamer("999", "stream.offline");
        Mockito.verify(trackingSubscriptionService, Mockito.times(2))
                .saveSubscription(Mockito.any());
    }

    @Test
    void trackStreamer_should_throwTrackingException_whenInvalidRequest() {
        TrackingRequestDto dto = TrackingRequestDto.builder().build();

        Assertions.assertThrows(TrackingException.class,
                () -> trackingService.trackStreamer(dto));
    }

    @Test
    void unsubscribe_should_deleteStreamerAndUnsubscribeFromTwitch_whenCalled() {
        StreamerEntity streamer = StreamerEntity.builder()
                .username(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .providerUserId(PROVIDER_USER_ID)
                .profileImageUrl(PROFILE_IMAGE_URL)
                .build();
        StreamerEntity saved = streamerRepository.save(streamer);

        UUID subscriptionId = UUID.randomUUID();
        StreamTrackingSubscriptionEntity sub = StreamTrackingSubscriptionEntity.builder()
                .subscriptionId(subscriptionId)
                .subscriptionType("stream.online")
                .streamerId(saved.getId())
                .build();

        Mockito.when(trackingSubscriptionService.getAllSubscriptionsByStreamerId(saved.getId()))
                .thenReturn(List.of(sub));

        trackingService.unsubscribe(saved.getId().toString());

        Mockito.verify(twitchApiClient).unsubscribeFromStreamer(subscriptionId, "stream.online");
        Mockito.verify(trackingSubscriptionService).deleteSubscription(
                Mockito.argThat(dto -> dto.getSubscriptionId().equals(subscriptionId))
        );
        Assertions.assertEquals(0, streamerRepository.count());
    }

    @Test
    void trackStreamer_should_rethrowException_whenWebhookSubscriptionFails() {
        Mockito.when(twitchApiClient.getStreamerInfo(STREAMER_USERNAME)).thenReturn(streamerInfo);
        Mockito.when(twitchApiClient.subscribeToStreamer(PROVIDER_USER_ID, "stream.online"))
                .thenThrow(new TwitchApiClientException("Twitch API unavailable"));

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .build();

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> trackingService.trackStreamer(dto));
    }
}