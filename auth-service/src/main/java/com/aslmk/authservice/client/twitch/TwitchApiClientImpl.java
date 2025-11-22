package com.aslmk.authservice.client.twitch;

import com.aslmk.authservice.exception.TwitchApiClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class TwitchApiClientImpl implements TwitchApiClient {
    @Value("${spring.security.oauth2.client.registration.twitch.client-id}")
    private String CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.twitch.client-secret}")
    private String CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.provider.twitch.token-uri}")
    private String TOKEN_UPDATE_URI;

    private final RestClient restClient;

    public TwitchApiClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public TwitchTokenRefreshResponse refreshTokens(String refreshToken) {
        MultiValueMap<String, String> requestBody = buildTokenRefreshRequestBody(refreshToken);

        try {
            return restClient.post()
                    .uri(TOKEN_UPDATE_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(TwitchTokenRefreshResponse.class)
                    .getBody();
        } catch (RestClientException e) {
            log.error("Error occurred while refreshing token", e);
            throw new TwitchApiClientException("Failed to refresh tokens", e);
        }
    }

    private MultiValueMap<String, String> buildTokenRefreshRequestBody(String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");
        return body;
    }
}