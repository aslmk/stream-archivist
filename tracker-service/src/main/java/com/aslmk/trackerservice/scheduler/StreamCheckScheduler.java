package com.aslmk.trackerservice.scheduler;

import com.aslmk.trackerservice.entity.Streamer;
import com.aslmk.trackerservice.repository.StreamerRepository;
import com.aslmk.trackerservice.service.StreamCheckerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StreamCheckScheduler {

    private final Map<String, Boolean> lastKnownStatus = new HashMap<>();

    private final StreamerRepository streamerRepository;
    private final StreamCheckerRegistry checkerRegistry;

    public StreamCheckScheduler(StreamerRepository streamerRepository, StreamCheckerRegistry checkerRegistry) {
        this.streamerRepository = streamerRepository;
        this.checkerRegistry = checkerRegistry;
    }

    @Scheduled(fixedRate = 3_600_000) // 1 hour
    public void checkStream() {

        for (Streamer streamer : streamerRepository.findAll()) {
            boolean isLiveNow = checkerRegistry
                    .getPlatformStreamChecker(String.valueOf(streamer.getPlatform()))
                    .isLive(streamer.getUsername());

            Boolean previousStatus = lastKnownStatus.get(streamer.getUsername());

            if (previousStatus == null || previousStatus != isLiveNow) {
                log.info("🔔 {} is now {} on {}", streamer.getUsername(), isLiveNow ? "LIVE" : "OFFLINE", streamer.getPlatform());
                lastKnownStatus.put(streamer.getUsername(), isLiveNow);
            }
        }
    }
}
