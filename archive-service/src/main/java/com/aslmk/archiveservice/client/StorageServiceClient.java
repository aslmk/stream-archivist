package com.aslmk.archiveservice.client;

import com.aslmk.archiveservice.dto.RecordingDownloadRequest;
import com.aslmk.archiveservice.dto.RecordingDownloadsResponse;

import java.util.UUID;

public interface StorageServiceClient {
    RecordingDownloadsResponse getRecordingDownloads(UUID streamerId,
                                                     RecordingDownloadRequest request);
}
