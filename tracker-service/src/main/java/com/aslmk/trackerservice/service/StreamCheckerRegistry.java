package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.exception.InvalidStreamingPlatformException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamCheckerRegistry {
    private final Map<String, PlatformStreamChecker> registry = new ConcurrentHashMap<>();

    public StreamCheckerRegistry(List<PlatformStreamChecker> checkers) {
        for (PlatformStreamChecker checker : checkers) {
            registry.put(checker.getPlatformName().toLowerCase(), checker);
        }
    }

    public PlatformStreamChecker getPlatformStreamChecker(String platform) {
        return Optional.ofNullable(registry.get(platform.toLowerCase()))
                .orElseThrow(() -> new InvalidStreamingPlatformException("Unsupported platform: " + platform));
    }
}
