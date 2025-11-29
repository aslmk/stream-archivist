package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.repository.StreamerRepository;
import com.aslmk.trackerservice.service.StreamerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class StreamerServiceImpl implements StreamerService {
    private final StreamerRepository streamerRepository;

    public StreamerServiceImpl(StreamerRepository streamerRepository) {
        this.streamerRepository = streamerRepository;
    }

    @Override
    public Optional<StreamerEntity> findByUsername(String username) {
        log.debug("Searching streamer by username='{}'", username);
        Optional<StreamerEntity> result = streamerRepository.findByUsername(username);

        if (result.isPresent()) {
            log.debug("Streamer found by username='{}': id={}", username, result.get().getId());
        } else {
            log.debug("Streamer not found by username='{}'", username);
        }

        return result;
    }

    @Override
    public Optional<StreamerEntity> findByProviderUserIdAndProviderName(String id, String providerName) {
        log.debug("Searching streamer by providerUserId='{}', providerName='{}'", id, providerName);
        Optional<StreamerEntity> result =
                streamerRepository.findByProviderUserIdAndProviderName(id, providerName);

        if (result.isPresent()) {
            log.debug("Streamer found by providerUserId='{}' and providerName='{}': username='{}'",
                    id, providerName, result.get().getUsername());
        } else {
            log.debug("Streamer not found by providerUserId='{}' and providerName='{}'",
                    id, providerName);
        }

        return result;
    }

    @Override
    public void create(CreateStreamerDto dto) {
        log.info("Creating new streamer: username='{}', provider='{}', streamerId='{}'",
                dto.getUsername(),
                dto.getProviderName(),
                dto.getStreamerId());

        StreamerEntity streamerEntity = StreamerEntity.builder()
                .username(dto.getUsername())
                .providerName(dto.getProviderName())
                .providerUserId(dto.getStreamerId())
                .build();
        streamerRepository.save(streamerEntity);

        log.info("Streamer created successfully: username='{}', provider='{}', streamerId='{}'",
                dto.getUsername(), dto.getProviderName(), dto.getStreamerId());
    }

    @Override
    public void updateUsername(StreamerEntity entity, String username) {
        String oldUsername = entity.getUsername();

        log.info("Updating streamer username: old='{}', new='{}', provider='{}', streamerId='{}'",
                oldUsername,
                username,
                entity.getProviderName(),
                entity.getProviderUserId());

        entity.setUsername(username);
        streamerRepository.save(entity);

        log.info("Username updated successfully for streamerId='{}'",
                entity.getProviderUserId());
    }
}
