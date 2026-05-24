package com.aslmk.archiveservice.service;

import com.aslmk.archiveservice.client.RecordingOrchestratorClient;
import com.aslmk.archiveservice.client.StorageServiceClient;
import com.aslmk.archiveservice.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ArchiveServiceImpl implements ArchiveService {

    private final RecordingOrchestratorClient orchestratorClient;
    private final StorageServiceClient storageClient;

    public ArchiveServiceImpl(RecordingOrchestratorClient orchestratorClient,
                              StorageServiceClient storageClient) {
        this.orchestratorClient = orchestratorClient;
        this.storageClient = storageClient;
    }

    @Override
    public StreamRecordings getStreamRecordings(UUID streamerId) {
        StreamListResponse orchestratorResponse = orchestratorClient.findStreamIdsByStreamerId(streamerId);

        List<UUID> streamIds = orchestratorResponse.streams().stream()
                .map(StreamReference::streamId)
                .toList();

        RecordingDownloadRequest request = RecordingDownloadRequest.builder()
                .streamIds(streamIds)
                .build();

        RecordingDownloadsResponse storageResponse = storageClient.getRecordingDownloads(streamerId, request);

        return StreamRecordings.builder()
                .streamerId(streamerId)
                .recordings(storageResponse.recordings())
                .build();
    }
}
