package com.aslmk.trackerservice.youtube;

import com.aslmk.trackerservice.dto.YoutubeLiveStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class YoutubeApiClient {
    @Value("${stream-tracker.youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final YoutubeChannelIdResolver youtubeChannelIdResolver;

    public YoutubeApiClient(RestTemplate restTemplate, YoutubeChannelIdResolver youtubeChannelIdResolver) {
        this.restTemplate = restTemplate;
        this.youtubeChannelIdResolver = youtubeChannelIdResolver;
    }

    public boolean isChannelLive(String broadcasterUsername) {
        String channelId = youtubeChannelIdResolver.resolveChannelIdFromHandle(broadcasterUsername);

        String url = "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet" +
                "&channelId=" + channelId +
                "&eventType=live" +
                "&type=video" +
                "&key=" + apiKey;

        try {
            YoutubeLiveStatusDto response = restTemplate.getForObject(url, YoutubeLiveStatusDto.class);

            if (response == null || response.getItems() == null) {
                throw new RestClientException("Could not resolve channel id from Youtube");
            }

            return response.getItems().length > 0;

        } catch (RestClientException e) {
            log.warn("Failed to check live status for channel '{}': {}", channelId, e.getMessage());
            return false;
        }
    }
}
