package com.aslmk.authservice;

import com.aslmk.authservice.client.twitch.TwitchApiClient;
import com.aslmk.authservice.client.twitch.TwitchApiClientImpl;
import com.aslmk.authservice.client.twitch.TwitchTokenRefreshResponse;
import com.aslmk.authservice.config.AppConfig;
import com.aslmk.authservice.exception.TwitchApiClientException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test")
@SpringBootTest(classes = {TwitchApiClientImpl.class, AppConfig.class})
@WireMockTest(httpPort = 8811)
public class TwitchApiClientIntegrationTests {

    private static final String TOKEN_URI = "/oauth2/token";
    private static final String ACCESS_TOKEN = "o4ooirh3qorhw0fhads0fhs0dfhasd0f";
    private static final String REFRESH_TOKEN = "jaifhjds0fahs8e0fh302hf3p2ifhdspfha0dsfh302";
    private static final Integer EXPIRES_IN = 3600;

    @Autowired
    private TwitchApiClient apiClient;

    @Test
    void should_refreshToken_when_refreshTokenIsValid() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_URI))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(buildApiResponse(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN))
                )
        );

        TwitchTokenRefreshResponse apiResponse = apiClient.refreshTokens("valid_refresh_token");

        Assertions.assertAll(
                () -> Assertions.assertEquals(ACCESS_TOKEN, apiResponse.getAccessToken()),
                () -> Assertions.assertEquals(REFRESH_TOKEN, apiResponse.getRefreshToken()),
                () -> Assertions.assertEquals(EXPIRES_IN, apiResponse.getExpiresIn()),
                () -> Assertions.assertEquals(List.of("user:read:email"), apiResponse.getScopes())
        );
    }

    @Test
    void should_throwTwitchApiClientException_whenRefreshTokenIsInvalid() {
        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_URI))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody(buildApiErrorResponse("Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid refresh token"))
                )
        );

        Assertions.assertThrows(TwitchApiClientException.class,
                () -> apiClient.refreshTokens("invalid_refresh_token"));
    }

    private String buildApiResponse(String accessToken, String refreshToken, int expiresIn) {
        return "{\n" +
                "    \"access_token\": \""+ accessToken + "\", \n" +
                "    \"expires_in\": " + expiresIn + ",\n" +
                "    \"refresh_token\": \"" + refreshToken + "\",\n" +
                "    \"scopes\": [\"user:read:email\"],\n" +
                "    \"token_type\": \"bearer\"\n" +
                "}";
    }

    private String buildApiErrorResponse(String error, int statusCode, String message) {
        return "{\n" +
                "    \"error\": \""+ error + "\", \n" +
                "    \"status\": " + statusCode + ",\n" +
                "    \"message\": \"" + message + "\"\n" +
                "}";
    }
}
