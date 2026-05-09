package com.aslmk.trackerservice.service.streamer;

import com.aslmk.trackerservice.dto.CreateStreamerDto;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.repository.StreamerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class StreamerServiceImpl implements StreamerService {
    private final StreamerRepository streamerRepository;

    public StreamerServiceImpl(StreamerRepository streamerRepository) {
        this.streamerRepository = streamerRepository;
    }

    @Override
    public Optional<StreamerEntity> findByUsername(String username) {
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
    public StreamerEntity create(CreateStreamerDto dto) {
        StreamerEntity streamerEntity = StreamerEntity.builder()
                .username(dto.getUsername())
                .providerName(dto.getProviderName())
                .profileImageUrl(dto.getProfileImageUrl())
                .providerUserId(dto.getStreamerId())
                .build();

        StreamerEntity streamer = streamerRepository.save(streamerEntity);

        log.debug("Streamer created successfully: username='{}', provider='{}', streamerId='{}'",
                dto.getUsername(), dto.getProviderName(), streamer.getId());

        return streamer;
    }

    @Override
    public void updateUsername(StreamerEntity entity, String username) {
        String oldUsername = entity.getUsername();
        entity.setUsername(username);
        streamerRepository.save(entity);
        log.debug("Username updated successfully for streamerId='{}': oldName='{}', newName='{}'",
                entity.getId(), oldUsername, username);
    }

    @Override
    public void deleteById(UUID id) {
        streamerRepository.deleteById(id);
    }
}
