package com.aslmk.trackerservice.client;

import com.aslmk.trackerservice.dto.UserInfoDto;
import com.aslmk.trackerservice.exception.AuthServiceClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AuthServiceClientImpl implements AuthServiceClient {

    @Value("${user.auth-service.base-url}")
    private String authServiceBaseUrl;

    private final RestClient restClient;

    public AuthServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String fetchUserAccessToken(UserInfoDto userInfo) {
        // TODO: implement /internal/{provider}/users/{provider_user_id}/token in auth-service
        validateUserInfo(userInfo);

        String uri = String.format("%s/internal/%s/users/%s/token",
                authServiceBaseUrl,
                userInfo.getProviderName(),
                userInfo.getProviderUserId()
                );

        try {
            return restClient.post()
                    .uri(uri)
                    .body(userInfo)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody();
        } catch (RestClientException e) {
            throw new AuthServiceClientException("Failed to fetch user's access_token from auth-service", e);
        }
    }

    private void validateUserInfo(UserInfoDto userInfo) {
        if (userInfo == null) {
            throw new AuthServiceClientException("User info is null");
        }

        if (userInfo.getProviderUserId() == null || userInfo.getProviderUserId().isBlank()) {
            throw new AuthServiceClientException("Provider user id cannot be null or blank");
        }

        if (userInfo.getProviderName() == null || userInfo.getProviderName().isBlank()) {
            throw new AuthServiceClientException("Provider name cannot be null or blank");
        }
    }
}
