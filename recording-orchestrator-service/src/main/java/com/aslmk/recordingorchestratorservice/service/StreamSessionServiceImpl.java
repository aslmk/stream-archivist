package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import com.aslmk.recordingorchestratorservice.dto.StreamSessionDto;
import com.aslmk.recordingorchestratorservice.repository.StreamSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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

}
