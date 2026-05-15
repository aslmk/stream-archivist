package com.aslmk.trackerservice.service.streamer;

import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.repository.StreamerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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
    public StreamerEntity create(CreateStreamerDto dto) {
        StreamerEntity streamerEntity = StreamerEntity.builder()
                .username(dto.getUsername())
                .providerName(dto.getProviderName())
                .profileImageUrl(dto.getProfileImageUrl())
                .providerUserId(dto.getStreamerId())
                .build();

        return streamerRepository.save(streamerEntity);
    }

    @Override
    public void updateUsername(StreamerEntity entity, String username) {
        entity.setUsername(username);
        streamerRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        streamerRepository.deleteById(id);
    }
}
