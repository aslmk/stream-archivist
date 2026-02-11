package com.aslmk.streamstatusservice.client;

import com.aslmk.common.dto.TrackedStreamerDto;
import com.aslmk.common.dto.TrackedStreamersResponse;
import com.aslmk.streamstatusservice.exception.SubscriptionServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SubscriptionServiceClientImpl implements SubscriptionServiceClient {

    private final WebClient subscriptionWebClient;

    public SubscriptionServiceClientImpl(WebClient subscriptionWebClient) {
        this.subscriptionWebClient = subscriptionWebClient;
    }

    @Override
    public Mono<List<TrackedStreamerDto>> getTrackedStreamers(UUID userId) {
        log.info("Fetching tracked streamers for user='{}'", userId);
        return subscriptionWebClient.get()
                .uri("/internal/users/{userId}/streamers", userId)
                .retrieve()
                .bodyToMono(TrackedStreamersResponse.class)
                .handle((response, sink) -> {
                    if (response == null || response.getStreamers() == null) {
                        log.error("Failed to get tracked streamers for user='{}': response is null", userId);
                        sink.error(new SubscriptionServiceClientException(
                                String.format("Could not get tracked streamers for user='%s': response is null", userId)));
                        return;
                    }
                    log.info("Successfully get tracked streamers for user='{}'", userId);
                    sink.next(response.getStreamers());
                });
    }
}
