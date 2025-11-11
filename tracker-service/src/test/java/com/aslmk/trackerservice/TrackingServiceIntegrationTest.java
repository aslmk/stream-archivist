package com.aslmk.trackerservice;

import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.repository.StreamerRepository;
import com.aslmk.trackerservice.service.impl.TrackingServiceImpl;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClient;
import org.junit.jupiter.api.Assertions;
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

import java.util.Optional;

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

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_QUALITY = "720p";
    private static final String PROVIDER_NAME = "twitch";
    private static final String PROVIDER_USER_ID = "123";

    @Test
    void should_doNothing_whenStreamerAlreadyTracked() {
        StreamerEntity existing = StreamerEntity.builder()
                .username(STREAMER_USERNAME)
                .providerName(PROVIDER_NAME)
                .providerUserId(STREAM_QUALITY)
                .build();
        streamerRepository.save(existing);

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamQuality(STREAM_QUALITY)
                .providerName(PROVIDER_NAME)
                .build();

        trackingService.trackStreamer(dto);

        Assertions.assertEquals(1, streamerRepository.count());
        Mockito.verify(twitchApiClient, Mockito.never()).getStreamerId(Mockito.any());
        Mockito.verify(twitchApiClient, Mockito.never()).subscribeToStreamer(Mockito.any());
    }

    @Test
    void should_updateUsername_whenStreamerExistsWithDifferentUsername() {
        StreamerEntity existing = StreamerEntity.builder()
                .username("old_test0")
                .providerName(PROVIDER_NAME)
                .providerUserId(PROVIDER_USER_ID)
                .build();
        streamerRepository.save(existing);

        Mockito.when(twitchApiClient.getStreamerId(STREAMER_USERNAME)).thenReturn(PROVIDER_USER_ID);

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamQuality(STREAM_QUALITY)
                .providerName(PROVIDER_NAME)
                .build();

        trackingService.trackStreamer(dto);

        Optional<StreamerEntity> updated = streamerRepository
                .findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME);

        Assertions.assertTrue(updated.isPresent());

        Assertions.assertEquals(STREAMER_USERNAME, updated.get().getUsername());
        Mockito.verify(twitchApiClient, Mockito.never()).subscribeToStreamer(Mockito.any());
    }

    @Test
    void should_createAndSubscribe_whenStreamerCompletelyNew() {
        Mockito.when(twitchApiClient.getStreamerId("test1")).thenReturn("999");

        TrackingRequestDto dto = TrackingRequestDto.builder()
                .streamerUsername("test1")
                .streamQuality(STREAM_QUALITY)
                .providerName(PROVIDER_NAME)
                .build();

        trackingService.trackStreamer(dto);



        StreamerEntity created = streamerRepository
                .findByProviderUserIdAndProviderName("999", PROVIDER_NAME).orElseThrow();

        Assertions.assertEquals("test1", created.getUsername());
        Mockito.verify(twitchApiClient).subscribeToStreamer("999");
    }

    @Test
    void should_throwTrackingException_whenInvalidRequest() {
        TrackingRequestDto dto = TrackingRequestDto.builder().build();

        Assertions.assertThrows(TrackingException.class,
                () -> trackingService.trackStreamer(dto));
    }
}

