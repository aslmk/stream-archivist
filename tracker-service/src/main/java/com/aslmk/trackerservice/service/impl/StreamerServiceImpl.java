package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.repository.StreamerRepository;
import com.aslmk.trackerservice.service.StreamerService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StreamerServiceImpl implements StreamerService {
    private final StreamerRepository streamerRepository;

    public StreamerServiceImpl(StreamerRepository streamerRepository) {
        this.streamerRepository = streamerRepository;
    }

    @Override
    public Optional<StreamerEntity> findByUsername(String username) {
        return streamerRepository.findByUsername(username);
    }

    @Override
    public Optional<StreamerEntity> findByProviderUserIdAndProviderName(String id, String providerName) {
        return streamerRepository.findByProviderUserIdAndProviderName(id, providerName);
    }

    @Override
    public void create(CreateStreamerDto dto) {
        StreamerEntity streamerEntity = StreamerEntity.builder()
                .username(dto.getUsername())
                .providerName(dto.getProviderName())
                .providerUserId(dto.getStreamerId())
                .build();
        streamerRepository.save(streamerEntity);
    }

    @Override
    public void updateUsername(StreamerEntity entity, String username) {
        entity.setUsername(username);
        streamerRepository.save(entity);
    }
}
