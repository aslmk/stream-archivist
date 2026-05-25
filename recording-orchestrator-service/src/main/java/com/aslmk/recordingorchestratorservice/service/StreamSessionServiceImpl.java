package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import com.aslmk.recordingorchestratorservice.dto.StreamListResponse;
import com.aslmk.recordingorchestratorservice.dto.StreamReference;
import com.aslmk.recordingorchestratorservice.dto.StreamSessionDto;
import com.aslmk.recordingorchestratorservice.dto.StreamSessionStatus;
import com.aslmk.recordingorchestratorservice.exception.StreamSessionNotFoundException;
import com.aslmk.recordingorchestratorservice.repository.StreamSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StreamSessionServiceImpl implements StreamSessionService {
    private final StreamSessionRepository repository;

    public StreamSessionServiceImpl(StreamSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public StreamSessionEntity save(StreamSessionDto dto) {
        StreamSessionEntity entity = StreamSessionEntity.builder()
                .streamerId(dto.getStreamerId())
                .status(dto.getStatus().getValue())
                .build();

        return repository.save(entity);
    }

    @Override
    public StreamSessionEntity getByStreamId(UUID streamId) {
        return repository.findByStreamId(streamId)
                .orElseThrow(() -> new StreamSessionNotFoundException(String
                        .format("Stream session not found: streamId='%s'", streamId))
                );
    }

    @Override
    public void updateStatus(UUID streamId, StreamSessionStatus newStatus) {
        repository.updateStatus(streamId, newStatus.name());
    }

    @Override
    public StreamListResponse findStreamIdsByStreamerId(UUID streamerId) {
        return repository.findUploadedStreamRecordings(streamerId).stream()
                .map(session -> new StreamReference(session.getStreamId()))
                .collect(Collectors
                        .collectingAndThen(Collectors.toList(), StreamListResponse::new));
    }
}
