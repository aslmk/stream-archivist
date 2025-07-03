package com.aslmk.trackerservice.streamingPlatform.youtube;

import com.aslmk.trackerservice.dto.YoutubeChannelIdResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class YoutubeChannelIdResolver {
    @Value("${stream-tracker.youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public YoutubeChannelIdResolver(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("youtube-channel-ids")
    public String resolveChannelIdFromHandle(String handle) {
        log.info("Resolving channel id from handle {}", handle);

        String url = "https://www.googleapis.com/youtube/v3/channels" +
                "?part=id&forHandle="+ handle +
                "&key=" + apiKey;

        try {
            YoutubeChannelIdResponseDto response = restTemplate
                    .getForObject(url, YoutubeChannelIdResponseDto.class);

            if (response == null || response.getItems() == null) {
                throw new RestClientException("Could not resolve channel id from Youtube");
            }

            return response.getItems()[0].getId();

        } catch (RestClientException e) {
            log.warn("Failed to resolve channel id from Youtube for {}: {}", handle, e.getMessage());
            throw new RuntimeException("Error occurred while fetching channel ID. Please try again later");
        }
    }
}
