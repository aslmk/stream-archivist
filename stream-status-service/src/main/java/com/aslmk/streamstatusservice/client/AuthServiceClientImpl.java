package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.EntityIdResolveResponse;
import com.aslmk.streamstatusservice.exception.AuthServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class AuthServiceClientImpl implements AuthServiceClient {

    private final WebClient authWebClient;

    public AuthServiceClientImpl(WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    @Override
    public Mono<UUID> resolveUserId(String providerUserId, String providerName) {
        return authWebClient.get()
                .uri("/internal/users/resolve?providerUserId={providerUserId}&providerName={providerName}", providerUserId, providerName)
                .retrieve()
                .bodyToMono(EntityIdResolveResponse.class)
                .handle((response, sink) -> {
                    if (response == null || response.getEntityId() == null) {
                        log.error("Failed to resolve user (providerUserId='{}', provider='{}'): auth-service returned invalid response", providerUserId, providerName);
                        sink.error(new AuthServiceClientException("Auth-service returned invalid response"));
                        return;
                    }
                    log.debug("User resolved: providerUserId='{}', provider='{}'", providerUserId, providerName);
                    sink.next(response.getEntityId());
                });
    }
}
