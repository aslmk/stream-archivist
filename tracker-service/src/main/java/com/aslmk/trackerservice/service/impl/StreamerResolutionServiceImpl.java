package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.StreamerNotFoundException;
import com.aslmk.trackerservice.service.StreamerResolutionService;
import com.aslmk.trackerservice.service.StreamerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class StreamerResolutionServiceImpl implements StreamerResolutionService {
    private final StreamerService streamerService;

    public StreamerResolutionServiceImpl(StreamerService streamerService) {
        this.streamerService = streamerService;
    }

    @Override
    public UUID resolveStreamerId(String providerUserId, String providerName) {
        log.debug("Resolving streamer: providerUserId='{}', providerName='{}'", providerUserId, providerName);

        Optional<StreamerEntity> dbStreamer = streamerService
                .findByProviderUserIdAndProviderName(providerUserId, providerName);

        if (dbStreamer.isEmpty()) {
            log.debug("Streamer not found: providerUserId='{}', providerName='{}'",
                    providerUserId, providerName);
            throw new StreamerNotFoundException(
                    String.format("Streamer not found: providerUserId='%s', providerName='%s'",
                            providerUserId, providerName)
            );
        }

        StreamerEntity streamer = dbStreamer.get();

        log.debug("Resolved streamer: id='{}'", streamer.getId());
        return streamer.getId();
    }
}
