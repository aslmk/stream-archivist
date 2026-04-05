package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.StreamTrackingSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.exception.TwitchApiClientException;
import com.aslmk.trackerservice.repository.StreamTrackingSubscriptionRepository;
import com.aslmk.trackerservice.repository.WebhookSubscriptionRepository;
import com.aslmk.trackerservice.scheduler.WebhookSubscriptionsCreateJob;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class WebhookSubscriptionsCreateJobIntegrationTests {

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
    private WebhookSubscriptionsCreateJob job;

    @Autowired
    private WebhookSubscriptionRepository webhookSubscriptionRepository;

    @Autowired
    private StreamTrackingSubscriptionRepository streamTrackingSubscriptionRepository;

    @MockitoBean
    private TwitchApiClient twitchApiClient;

    private static final UUID STREAMER_INTERNAL_ID = UUID.randomUUID();
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";
    private static final String EVENT_TYPE_ONLINE = "stream.online";
    private static final String EVENT_TYPE_OFFLINE = "stream.offline";

    @BeforeEach
    void setUp() {
        streamTrackingSubscriptionRepository.deleteAll();
        webhookSubscriptionRepository.deleteAll();
    }

    private WebhookSubscriptionEntity buildUncreatedEntity(String eventType) {
        WebhookSubscriptionId id = WebhookSubscriptionId.builder()
                .streamerInternalId(STREAMER_INTERNAL_ID)
                .subscriptionType(eventType)
                .build();
        return WebhookSubscriptionEntity.builder()
                .id(id)
                .streamerProviderId(PROVIDER_USER_ID)
                .providerName(PROVIDER_NAME)
                .subscriptionId(null)
                .subscriptionStatus(WebhookSubscriptionStatus.PENDING.name())
                .retryCount(0)
                .build();
    }

    private TwitchWebhookSubscriptionResponse buildTwitchResponse(UUID subscriptionId, String eventType) {
        return TwitchWebhookSubscriptionResponse.builder()
                .id(subscriptionId)
                .type(eventType)
                .build();
    }

    @Test
    void createSubscriptions_should_doNothing_when_noUncreatedSubscriptionsExist() {
        job.createSubscriptions();

        Mockito.verify(twitchApiClient, Mockito.never()).subscribeToStreamer(Mockito.any(), Mockito.any());
        Assertions.assertEquals(0, streamTrackingSubscriptionRepository.count());
    }

    @Test
    void createSubscriptions_should_saveSubscriptionIdAndCreateTrackingRecord_when_singleUncreatedSubscriptionExists() {
        webhookSubscriptionRepository.save(buildUncreatedEntity(EVENT_TYPE_ONLINE));

        UUID providerSubscriptionId = UUID.randomUUID();
        Mockito.when(twitchApiClient.subscribeToStreamer(PROVIDER_USER_ID, EVENT_TYPE_ONLINE))
                .thenReturn(buildTwitchResponse(providerSubscriptionId, EVENT_TYPE_ONLINE));

        job.createSubscriptions();

        WebhookSubscriptionEntity updated = webhookSubscriptionRepository
                .findById(WebhookSubscriptionId.builder()
                        .streamerInternalId(STREAMER_INTERNAL_ID)
                        .subscriptionType(EVENT_TYPE_ONLINE)
                        .build())
                .orElseThrow();
        Assertions.assertEquals(providerSubscriptionId, updated.getSubscriptionId());

        Assertions.assertEquals(1, streamTrackingSubscriptionRepository.count());

        StreamTrackingSubscriptionEntity trackingRecord = streamTrackingSubscriptionRepository.findAll().iterator().next();
        Assertions.assertEquals(providerSubscriptionId, trackingRecord.getSubscriptionId());
        Assertions.assertEquals(EVENT_TYPE_ONLINE, trackingRecord.getSubscriptionType());
        Assertions.assertEquals(PROVIDER_NAME, trackingRecord.getProviderName());
        Assertions.assertEquals(STREAMER_INTERNAL_ID, trackingRecord.getStreamerId());
    }

    @Test
    void createSubscriptions_should_processAllSubscriptions_when_multipleUncreatedExist() {
        webhookSubscriptionRepository.save(buildUncreatedEntity(EVENT_TYPE_ONLINE));
        webhookSubscriptionRepository.save(buildUncreatedEntity(EVENT_TYPE_OFFLINE));

        UUID onlineSubId = UUID.randomUUID();
        UUID offlineSubId = UUID.randomUUID();

        Mockito.when(twitchApiClient.subscribeToStreamer(PROVIDER_USER_ID, EVENT_TYPE_ONLINE))
                .thenReturn(buildTwitchResponse(onlineSubId, EVENT_TYPE_ONLINE));
        Mockito.when(twitchApiClient.subscribeToStreamer(PROVIDER_USER_ID, EVENT_TYPE_OFFLINE))
                .thenReturn(buildTwitchResponse(offlineSubId, EVENT_TYPE_OFFLINE));

        job.createSubscriptions();

        Assertions.assertEquals(2, streamTrackingSubscriptionRepository.count());

        List<UUID> savedSubIds = StreamSupport
                .stream(webhookSubscriptionRepository.findAll().spliterator(), false)
                .map(WebhookSubscriptionEntity::getSubscriptionId)
                .toList();
        Assertions.assertTrue(savedSubIds.contains(onlineSubId));
        Assertions.assertTrue(savedSubIds.contains(offlineSubId));
    }

    @Test
    void createSubscriptions_should_continueProcessing_when_oneSubscriptionFails() {
        webhookSubscriptionRepository.save(buildUncreatedEntity(EVENT_TYPE_ONLINE));
        webhookSubscriptionRepository.save(buildUncreatedEntity(EVENT_TYPE_OFFLINE));

        UUID offlineSubId = UUID.randomUUID();

        Mockito.when(twitchApiClient.subscribeToStreamer(PROVIDER_USER_ID, EVENT_TYPE_ONLINE))
                .thenThrow(new TwitchApiClientException("Twitch API unavailable"));
        Mockito.when(twitchApiClient.subscribeToStreamer(PROVIDER_USER_ID, EVENT_TYPE_OFFLINE))
                .thenReturn(buildTwitchResponse(offlineSubId, EVENT_TYPE_OFFLINE));

        Assertions.assertDoesNotThrow(() -> job.createSubscriptions());

        Assertions.assertEquals(1, streamTrackingSubscriptionRepository.count());

        WebhookSubscriptionEntity failedEntity = webhookSubscriptionRepository
                .findById(WebhookSubscriptionId.builder()
                        .streamerInternalId(STREAMER_INTERNAL_ID)
                        .subscriptionType(EVENT_TYPE_ONLINE)
                        .build())
                .orElseThrow();
        Assertions.assertNull(failedEntity.getSubscriptionId());

        WebhookSubscriptionEntity succeededEntity = webhookSubscriptionRepository
                .findById(WebhookSubscriptionId.builder()
                        .streamerInternalId(STREAMER_INTERNAL_ID)
                        .subscriptionType(EVENT_TYPE_OFFLINE)
                        .build())
                .orElseThrow();
        Assertions.assertEquals(offlineSubId, succeededEntity.getSubscriptionId());
    }
}