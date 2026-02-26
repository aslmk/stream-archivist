package com.aslmk.trackerservice;

import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.TrackingException;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.impl.TrackingServiceImpl;
import com.aslmk.trackerservice.streamingPlatform.twitch.client.TwitchApiClient;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchStreamerInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceUnitTests {

    @Mock
    private TwitchApiClient twitchClient;

    @Mock
    private StreamerService streamerService;

    @InjectMocks
    private TrackingServiceImpl trackingService;

    private TrackingRequestDto validRequest;

    private static final String STREAMER_USERNAME = "test0";
    private static final String PROVIDER_NAME = "twitch";
    private static final String PROVIDER_USER_ID = "123";
    private static final String PROFILE_IMAGE_URL = "image-url";

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
                .profileImageUrl("profile-image-url")
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
                .thenReturn(Optional.of(new StreamerEntity()));

        trackingService.trackStreamer(validRequest);

        Mockito.verify(streamerService).findByUsername(STREAMER_USERNAME);

        Mockito.verifyNoMoreInteractions(streamerService, twitchClient);
    }

    @Test
    void should_updateUsername_when_streamerExistsByIdAndProvider() {
        Mockito.when(streamerService.findByUsername(STREAMER_USERNAME)).thenReturn(Optional.empty());
        Mockito.when(twitchClient.getStreamerInfo(STREAMER_USERNAME)).thenReturn(streamerInfo);
        Mockito.when(streamerService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME))
                .thenReturn(Optional.of(new StreamerEntity()));

        trackingService.trackStreamer(validRequest);

        Mockito.verify(streamerService).updateUsername(any(StreamerEntity.class), eq(STREAMER_USERNAME));
        Mockito.verify(twitchClient, never()).subscribeToStreamer(anyString());
        Mockito.verify(streamerService, never()).create(any());
    }

    @Test
    void should_createStreamer_when_notTrackedAndNotInDb() {
        Mockito.when(streamerService.findByUsername(STREAMER_USERNAME)).thenReturn(Optional.empty());
        Mockito.when(twitchClient.getStreamerInfo(STREAMER_USERNAME)).thenReturn(streamerInfo);
        Mockito.when(streamerService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME))
                .thenReturn(Optional.empty());
        Mockito.when(streamerService.create(Mockito.any())).thenReturn(validStreamerEntity);

        trackingService.trackStreamer(validRequest);

        Mockito.verify(twitchClient).subscribeToStreamer(PROVIDER_USER_ID);
        Mockito.verify(streamerService).create(argThat(dto ->
                dto.getUsername().equals(STREAMER_USERNAME) &&
                        dto.getStreamerId().equals(PROVIDER_USER_ID) &&
                        dto.getProviderName().equals(PROVIDER_NAME)));
    }
}
