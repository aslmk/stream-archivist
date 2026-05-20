package com.aslmk.storageservice.client;

import java.util.UUID;

public interface RecordingOrchestratorClient {
    void notifyUploadCompleted(UUID streamId);
}
