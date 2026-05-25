package com.aslmk.archiveservice.client;

import com.aslmk.archiveservice.dto.StreamListResponse;

import java.util.UUID;

public interface RecordingOrchestratorClient {
    StreamListResponse findStreamIdsByStreamerId(UUID streamerId);
}
