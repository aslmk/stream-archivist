package com.aslmk.archiveservice.service;

import com.aslmk.archiveservice.client.RecordingOrchestratorClient;
import com.aslmk.archiveservice.client.StorageServiceClient;
import com.aslmk.archiveservice.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
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
        log.debug("Retrieving recording archive",
                kv("streamerId", streamerId));

        StreamListResponse orchestratorResponse = orchestratorClient.findStreamIdsByStreamerId(streamerId);

        List<UUID> streamIds = orchestratorResponse.streams().stream()
                .map(StreamReference::streamId)
                .toList();

        if (streamIds.isEmpty()) {
            log.debug("No archived streams found",
                    kv("streamerId", streamerId));
            return StreamRecordings.builder()
                    .streamerId(streamerId)
                    .recordings(Collections.emptyList())
                    .build();
        }

        RecordingDownloadRequest request = RecordingDownloadRequest.builder()
                .streamIds(streamIds)
                .build();

        RecordingDownloadsResponse storageResponse = storageClient.getRecordingDownloads(streamerId, request);

        log.debug("Retrieved recording archive",
                kv("streamerId", streamerId),
                kv("streamRecordingsCount", storageResponse.recordings().size()));

        return StreamRecordings.builder()
                .streamerId(streamerId)
                .recordings(storageResponse.recordings())
                .build();
    }
}
