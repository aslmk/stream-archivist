package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.TwitchApiClient;
import com.aslmk.trackerservice.client.twitch.dto.TwitchWebhookSubscriptionResponse;
import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.WebhookSubscriptionDto;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.repository.WebhookSubscriptionRepository;
import com.aslmk.trackerservice.scheduler.WebhookSubscriptionsStatusCheckJob;
import com.aslmk.trackerservice.service.subscription.WebhookSubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

import java.util.UUID;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class WebhookSubscriptionsStatusCheckJobIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test-db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebhookSubscriptionsStatusCheckJob job;

    @Autowired
    private WebhookSubscriptionService webhookSubscriptionService;

    @Autowired
    private WebhookSubscriptionRepository repository;

    @MockitoBean
    private TwitchApiClient apiClient;

    private static final String SUBSCRIPTION_TYPE = "stream.online";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";
    private static final int MAX_RETRY_COUNT = 5;

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    private WebhookSubscriptionId savePendingSubscription(UUID streamerId, UUID subscriptionId, int retryCount) {
        webhookSubscriptionService.saveSubscription(WebhookSubscriptionDto.builder()
                .streamerInternalId(streamerId)
                .subscriptionType(SUBSCRIPTION_TYPE)
                .streamerProviderId(PROVIDER_USER_ID)
                .providerName(PROVIDER_NAME)
                .subscriptionId(subscriptionId)
                .subscriptionStatus(WebhookSubscriptionStatus.PENDING.name())
                .retryCount(retryCount)
                .build());

        return WebhookSubscriptionId.builder()
                .streamerInternalId(streamerId)
                .subscriptionType(SUBSCRIPTION_TYPE)
                .build();
    }

    private WebhookSubscriptionId saveFailedSubscription(UUID streamerId, UUID subscriptionId) {
        webhookSubscriptionService.saveSubscription(WebhookSubscriptionDto.builder()
                .streamerInternalId(streamerId)
                .subscriptionType(SUBSCRIPTION_TYPE)
                .streamerProviderId(PROVIDER_USER_ID)
                .providerName(PROVIDER_NAME)
                .subscriptionId(subscriptionId)
                .subscriptionStatus(WebhookSubscriptionStatus.FAILED.name())
                .retryCount(MAX_RETRY_COUNT)
                .build());

        return WebhookSubscriptionId.builder()
                .streamerInternalId(streamerId)
                .subscriptionType(SUBSCRIPTION_TYPE)
                .build();
    }

    @Test
    void checkSubscriptionsStatus_should_markFailed_when_retryCountReachedMax() {
        WebhookSubscriptionId id = savePendingSubscription(UUID.randomUUID(), UUID.randomUUID(), MAX_RETRY_COUNT);

        job.checkSubscriptionsStatus();

        WebhookSubscriptionEntity updated = repository.findById(id).orElseThrow();
        Assertions.assertEquals(WebhookSubscriptionStatus.FAILED.name(), updated.getSubscriptionStatus());
        Mockito.verifyNoInteractions(apiClient);
    }

    @Test
    void checkSubscriptionsStatus_should_updateStatusToEnabled_when_twitchStatusIsEnabled() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = savePendingSubscription(UUID.randomUUID(), subscriptionId, 0);

        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenReturn(TwitchWebhookSubscriptionResponse.builder()
                        .status(WebhookSubscriptionStatus.ENABLED.getValue())
                        .build());

        job.checkSubscriptionsStatus();

        WebhookSubscriptionEntity updated = repository.findById(id).orElseThrow();
        Assertions.assertEquals(WebhookSubscriptionStatus.ENABLED.name(), updated.getSubscriptionStatus());
    }

    @Test
    void checkSubscriptionsStatus_should_keepStatusPending_when_twitchStatusIsPending() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = savePendingSubscription(UUID.randomUUID(), subscriptionId, 0);

        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenReturn(TwitchWebhookSubscriptionResponse.builder()
                        .status(WebhookSubscriptionStatus.PENDING.getValue())
                        .build());

        job.checkSubscriptionsStatus();

        WebhookSubscriptionEntity updated = repository.findById(id).orElseThrow();
        Assertions.assertEquals(WebhookSubscriptionStatus.PENDING.name(), updated.getSubscriptionStatus());
        Assertions.assertEquals(0, updated.getRetryCount());
    }

    @Test
    void checkSubscriptionsStatus_should_resetSubscriptionIdAndIncrementRetryCount_when_twitchStatusIsFailed() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = savePendingSubscription(UUID.randomUUID(), subscriptionId, 0);

        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenReturn(TwitchWebhookSubscriptionResponse.builder()
                        .status(WebhookSubscriptionStatus.FAILED.getValue())
                        .build());

        job.checkSubscriptionsStatus();

        WebhookSubscriptionEntity updated = repository.findById(id).orElseThrow();
        Assertions.assertNull(updated.getSubscriptionId());
        Assertions.assertEquals(1, updated.getRetryCount());
        Mockito.verify(apiClient).unsubscribeFromStreamer(subscriptionId, SUBSCRIPTION_TYPE);
    }

    @Test
    void checkSubscriptionsStatus_should_onlyIncrementRetryCount_when_exceptionThrownAndBelowMax() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = savePendingSubscription(UUID.randomUUID(), subscriptionId, 1);

        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenThrow(new RuntimeException("Twitch API error"));

        job.checkSubscriptionsStatus();

        WebhookSubscriptionEntity updated = repository.findById(id).orElseThrow();
        Assertions.assertEquals(2, updated.getRetryCount());
        Assertions.assertEquals(WebhookSubscriptionStatus.PENDING.name(), updated.getSubscriptionStatus());
    }

    @Test
    void checkSubscriptionsStatus_should_incrementRetryCountAndMarkFailed_when_exceptionThrownAndReachesMax() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = savePendingSubscription(UUID.randomUUID(),
                subscriptionId, MAX_RETRY_COUNT - 1);

        Mockito.when(apiClient.getSubscriptionInfo(subscriptionId))
                .thenThrow(new RuntimeException("Twitch API error"));

        job.checkSubscriptionsStatus();

        WebhookSubscriptionEntity updated = repository.findById(id).orElseThrow();
        Assertions.assertEquals(MAX_RETRY_COUNT, updated.getRetryCount());
        Assertions.assertEquals(WebhookSubscriptionStatus.FAILED.name(), updated.getSubscriptionStatus());
    }

    @Test
    void deleteFailedSubscriptions_should_deleteSubscription_when_apiCallSucceeds() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = saveFailedSubscription(UUID.randomUUID(), subscriptionId);

        job.deleteFailedSubscriptions();

        Assertions.assertFalse(repository.findById(id).isPresent());
        Mockito.verify(apiClient).unsubscribeFromStreamer(subscriptionId, SUBSCRIPTION_TYPE);
    }

    @Test
    void deleteFailedSubscriptions_should_keepSubscription_when_apiThrowsException() {
        UUID subscriptionId = UUID.randomUUID();
        WebhookSubscriptionId id = saveFailedSubscription(UUID.randomUUID(), subscriptionId);

        Mockito.doThrow(new RuntimeException("Twitch API error"))
                .when(apiClient).unsubscribeFromStreamer(subscriptionId, SUBSCRIPTION_TYPE);

        job.deleteFailedSubscriptions();

        Assertions.assertTrue(repository.findById(id).isPresent());
    }
}