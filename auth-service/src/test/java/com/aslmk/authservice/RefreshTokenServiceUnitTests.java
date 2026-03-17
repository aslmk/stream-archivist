package com.aslmk.authservice;

import com.aslmk.authservice.domain.auth.RefreshTokenEntity;
import com.aslmk.authservice.exception.InvalidRefreshTokenException;
import com.aslmk.authservice.exception.RefreshTokenExpiredException;
import com.aslmk.authservice.exception.RefreshTokenNotFoundException;
import com.aslmk.authservice.repository.RefreshTokenRepository;
import com.aslmk.authservice.service.token.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceUnitTests {

    @Mock
    private RefreshTokenRepository repository;

    @Mock
    private Clock clock;

    @InjectMocks
    private RefreshTokenServiceImpl service;

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2026, 3, 15,
            19, 0, 0, 0,
            ZoneId.of("UTC")
    );

    private static final String JWT_REFRESH_TOKEN = "jwt-refresh-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshTokenLifetime", Duration.ofDays(7));
    }

    @Test
    void delete_should_throwInvalidRefreshTokenException_when_tokenIsNull() {
        Assertions.assertThrows(InvalidRefreshTokenException.class,
                () -> service.delete(null));

        Mockito.verify(repository, Mockito.never()).deleteByTokenHash(Mockito.any());
    }

    @Test
    void validate_should_throwInvalidRefreshTokenException_when_tokenIsNull() {
        Assertions.assertThrows(InvalidRefreshTokenException.class,
                () -> service.validate(null));

        Mockito.verify(repository, Mockito.never()).findByTokenHash(Mockito.any());
    }

    @Test
    void delete_should_throwInvalidRefreshTokenException_when_tokenIsEmpty() {
        Assertions.assertThrows(InvalidRefreshTokenException.class,
                () -> service.delete(""));

        Mockito.verify(repository, Mockito.never()).deleteByTokenHash(Mockito.any());
    }

    @Test
    void validate_should_throwInvalidRefreshTokenException_when_tokenIsEmpty() {
        Assertions.assertThrows(InvalidRefreshTokenException.class,
                () -> service.validate(""));

        Mockito.verify(repository, Mockito.never()).findByTokenHash(Mockito.any());
    }

    @Test
    void validate_should_returnEntity_when_tokenIsValidAndExistsInDatabase() {
        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());

        Duration refreshTokenLifetime = Duration.ofDays(7);

        String hashedToken = hash(JWT_REFRESH_TOKEN);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .tokenHash(hashedToken)
                .expiresAt(NOW.plusDays(refreshTokenLifetime.toDays()).toLocalDateTime())
                .build();

        Mockito.when(repository.findByTokenHash(hashedToken)).thenReturn(Optional.of(refreshToken));

        RefreshTokenEntity actual = service.validate(JWT_REFRESH_TOKEN);

        Assertions.assertEquals(actual.getTokenHash(), hashedToken);
    }

    @Test
    void validate_should_throwRefreshTokenExpiredException_when_tokenExpired() {
        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());

        String hashedToken = hash(JWT_REFRESH_TOKEN);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .tokenHash(hashedToken)
                .expiresAt(NOW.minusDays(1).toLocalDateTime())
                .build();

        Mockito.when(repository.findByTokenHash(hashedToken)).thenReturn(Optional.of(refreshToken));

        Assertions.assertThrows(RefreshTokenExpiredException.class,
                () -> service.validate(JWT_REFRESH_TOKEN));

        Mockito.verify(repository).deleteByTokenHash(hashedToken);
    }

    @Test
    void validate_should_throwRefreshTokenNotFoundException_whenTokenDoesNotExist() {
        Mockito.when(repository.findByTokenHash(Mockito.any())).thenReturn(Optional.empty());
        Assertions.assertThrows(RefreshTokenNotFoundException.class,
                () -> service.validate(JWT_REFRESH_TOKEN));
    }

    @Test
    void delete_should_deleteRefreshToken() {
        String hashedToken = hash(JWT_REFRESH_TOKEN);

        service.delete(JWT_REFRESH_TOKEN);

        Mockito.verify(repository).deleteByTokenHash(hashedToken);
    }

    @Test
    void generate_should_returnRefreshToken() {
        UUID userId = UUID.randomUUID();
        Duration refreshTokenLifetime = Duration.ofDays(7);

        Mockito.when(clock.getZone()).thenReturn(NOW.getZone());
        Mockito.when(clock.instant()).thenReturn(NOW.toInstant());

        RefreshTokenEntity validEntity = RefreshTokenEntity.builder()
                .userId(userId)
                .expiresAt(NOW.plusDays(refreshTokenLifetime.toDays()).toLocalDateTime())
                .build();

        String actualRefreshToken = service.generate(userId);
        String hashedToken = hash(actualRefreshToken);

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);

        Mockito.verify(repository).save(captor.capture());

        RefreshTokenEntity actualEntity = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(validEntity.getUserId(), actualEntity.getUserId()),
                () -> Assertions.assertEquals(validEntity.getExpiresAt(), actualEntity.getExpiresAt()),
                () -> Assertions.assertEquals(hashedToken, actualEntity.getTokenHash())
        );
    }

    private String hash(String rawRefreshToken) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = sha256.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.toBase64String(hashedToken);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
