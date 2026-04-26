package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.StreamSessionEntity;
import com.aslmk.storageservice.dto.StreamSessionData;
import com.aslmk.storageservice.repository.StreamSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class StreamSessionServiceImpl implements StreamSessionService {
    private final StreamSessionRepository repository;

    public StreamSessionServiceImpl(StreamSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveIfNotExists(StreamSessionData data) {
        try {
            StreamSessionEntity entity = StreamSessionEntity.builder()
                    .streamId(data.streamId())
                    .uploadId(data.uploadId())
                    .s3ObjectKey(data.key())
                    .build();

            repository.save(entity);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                log.warn("Duplicate entity: streamId='{}'", data.streamId());
            }
            throw e;
        }
    }

    @Override
    public Optional<StreamSessionEntity> getByStreamId(UUID streamId) {
        return repository.findByStreamId(streamId);
    }

    @Override
    public String getUploadId(UUID streamId) {
        return repository.findByStreamId(streamId)
                .orElseThrow(() -> new RuntimeException(
                        String.format("No uploadId found for streamId='%s'",
                                streamId)))
                .getUploadId();
    }

    @Override
    public void removeByStreamId(UUID streamId) {
        repository.removeByStreamId(streamId);
    }
}
