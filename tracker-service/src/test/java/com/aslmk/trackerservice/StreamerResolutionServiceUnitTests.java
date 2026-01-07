package com.aslmk.trackerservice;

import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.StreamerNotFoundException;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.impl.StreamerResolutionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StreamerResolutionServiceUnitTests {

    @Mock
    private StreamerService streamerService;

    @InjectMocks
    private StreamerResolutionServiceImpl resolutionService;

    private static final String PROVIDER_USER_ID = "123";
    private static final String PROVIDER_NAME = "twitch";

    @Test
    void should_resolveStreamerId_when_streamerExists() {
        UUID streamerId = UUID.randomUUID();

        StreamerEntity streamer = StreamerEntity.builder()
                .id(streamerId)
                .build();

        Mockito.when(streamerService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME))
                .thenReturn(Optional.of(streamer));

        UUID actualResult = resolutionService.resolveStreamerId(PROVIDER_USER_ID, PROVIDER_NAME);

        Assertions.assertEquals(streamerId, actualResult);
    }

    @Test
    void should_throwStreamerNotFoundException_when_streamerNotFound() {
        Mockito.when(streamerService.findByProviderUserIdAndProviderName(PROVIDER_USER_ID, PROVIDER_NAME))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(StreamerNotFoundException.class,
                () -> resolutionService.resolveStreamerId(PROVIDER_USER_ID, PROVIDER_NAME));
    }
}
