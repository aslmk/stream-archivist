package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import com.aslmk.recordingorchestratorservice.dto.StreamSessionDto;
import com.aslmk.recordingorchestratorservice.dto.StreamSessionStatus;

import java.util.UUID;

public interface StreamSessionService {
    StreamSessionEntity save(StreamSessionDto dto);
    StreamSessionEntity getByStreamId(UUID streamerId);
    void updateStatus(UUID streamId, StreamSessionStatus newStatus);
}
