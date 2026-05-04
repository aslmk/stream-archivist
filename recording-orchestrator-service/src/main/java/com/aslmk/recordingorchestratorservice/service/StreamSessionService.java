package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import com.aslmk.recordingorchestratorservice.dto.StreamSessionDto;

public interface StreamSessionService {
    StreamSessionEntity save(StreamSessionDto dto);
}
