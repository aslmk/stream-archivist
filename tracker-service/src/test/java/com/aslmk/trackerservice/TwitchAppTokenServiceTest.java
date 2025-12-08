package com.aslmk.trackerservice;

import com.aslmk.trackerservice.entity.TwitchAppTokenEntity;
import com.aslmk.trackerservice.repository.TwitchAppTokenRepository;
import com.aslmk.trackerservice.service.impl.TwitchAppTokenServiceImpl;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchAppAccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TwitchAppTokenServiceTest {

    @Mock
    private TwitchAppTokenRepository repository;

    @Mock
    private Clock clock;

    @InjectMocks
    private TwitchAppTokenServiceImpl service;

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2025, 12, 8,
            12, 0, 0, 0,
            ZoneId.of("UTC")
    );

    @Test
    void getAppAccessToken_should_returnToken_when_tokenExists() {
        TwitchAppTokenEntity entity = TwitchAppTokenEntity.builder()
                .id(UUID.randomUUID())
                .accessToken("test_token")
                .expiresAt(LocalDateTime.of(2025, 12, 10, 12, 0))
                .tokenType("bearer")
                .build();

        Mockito.when(repository.findTopByOrderByIdAsc()).thenReturn(Optional.of(entity));

        Optional<TwitchAppTokenEntity> result = service.getAppAccessToken();

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("test_token", result.get().getAccessToken());
        Mockito.verify(repository).findTopByOrderByIdAsc();
    }

    @Test
    void getAppAccessToken_should_returnEmpty_when_noTokenExists() {
        Mockito.when(repository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());

        Optional<TwitchAppTokenEntity> result = service.getAppAccessToken();

        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(repository).findTopByOrderByIdAsc();
    }

    @Test
    void save_should_saveTokenWithCorrectExpiresAt() {
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());
        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());

        TwitchAppAccessToken appToken = TwitchAppAccessToken.builder()
                .accessToken("new_token")
                .expiresIn(3600) // 1 hour
                .tokenType("bearer")
                .build();

        service.save(appToken);

        ArgumentCaptor<TwitchAppTokenEntity> captor = ArgumentCaptor.forClass(TwitchAppTokenEntity.class);
        Mockito.verify(repository).save(captor.capture());

        TwitchAppTokenEntity saved = captor.getValue();
        Assertions.assertEquals("new_token", saved.getAccessToken());
        Assertions.assertEquals("bearer", saved.getTokenType());
        Assertions.assertEquals(NOW.plusSeconds(3600).toLocalDateTime(), saved.getExpiresAt());
    }

    @Test
    void update_should_updateExistingToken() {
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());
        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());

        TwitchAppTokenEntity currentToken = TwitchAppTokenEntity.builder()
                .id(UUID.randomUUID())
                .accessToken("old_token")
                .expiresAt(LocalDateTime.of(2025, 12, 7, 10, 0))
                .tokenType("bearer")
                .build();

        TwitchAppAccessToken newAppToken = TwitchAppAccessToken.builder()
                .accessToken("updated_token")
                .expiresIn(7200) // 2 hours
                .tokenType("bearer")
                .build();

        service.update(currentToken, newAppToken);

        Mockito.verify(repository).save(currentToken);
        Assertions.assertEquals("updated_token", currentToken.getAccessToken());
        Assertions.assertEquals(NOW.plusSeconds(7200).toLocalDateTime(), currentToken.getExpiresAt());
        Assertions.assertEquals("bearer", currentToken.getTokenType());
    }

}