package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.EntityIdResolveResponse;
import com.aslmk.streamstatusservice.exception.AuthServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Slf4j
@Service
public class AuthServiceClientImpl implements AuthServiceClient {

    private final RestClient restClient;

    @Value("${user.auth-service.url}")
    private String authServiceUrl;

    public AuthServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public UUID resolveUserId(String providerUserId, String providerName) {
        log.debug("Resolving user: providerUserId='{}', provider='{}'", providerUserId, providerName);
        try {
            EntityIdResolveResponse response = restClient.get()
                    .uri(UriComponentsBuilder.fromUriString(authServiceUrl)
                            .queryParam("providerUserId", providerUserId)
                            .queryParam("providerName", providerName)
                            .build()
                            .toUri())
                    .retrieve()
                    .toEntity(EntityIdResolveResponse.class)
                    .getBody();

            if (response == null || response.getEntityId() == null) {
                log.error("Failed to resolve user (providerUserId='{}', provider='{}'): auth-service returned invalid response", providerUserId, providerName);
                throw new AuthServiceClientException("Auth-service returned invalid response");
            }

            log.debug("User resolved: providerUserId='{}', provider='{}'", providerUserId, providerName);
            return response.getEntityId();
        } catch (RestClientException e) {
            throw new AuthServiceClientException("Failed to resolve user via auth-service", e);
        }
    }
}
