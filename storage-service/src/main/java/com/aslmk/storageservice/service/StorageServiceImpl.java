package com.aslmk.storageservice.service;

import com.aslmk.storageservice.client.RecordingOrchestratorClient;
import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.*;
import com.aslmk.storageservice.repository.StorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;
    private final UploadSessionService uploadSessionService;
    private final RecordingOrchestratorClient apiClient;

    public StorageServiceImpl(StorageRepository storageRepository,
                              UploadSessionService uploadSessionService,
                              RecordingOrchestratorClient apiClient) {
        this.storageRepository = storageRepository;
        this.uploadSessionService = uploadSessionService;
        this.apiClient = apiClient;
    }

    @Override
    public InitUploadingResponse initUpload(InitUploadingRequest request) {
        log.debug("Initiating multipart upload",
                kv("streamId", request.streamId()),
                kv("filename", request.fileName()));

        String s3key = buildS3ObjectKey(request.streamId(), request.fileName());

        String uploadId;
        Optional<UploadSessionEntity> session = uploadSessionService
                .findByStreamId(request.streamId());

        if (session.isPresent()) {
            uploadId = session.get().getUploadId();
            log.debug("Found uploadId in the database",
                    kv("streamId", request.streamId()),
                    kv("uploadId", uploadId),
                    kv("s3Key", s3key));
        } else {
            uploadId = storageRepository.generateUploadId(s3key);

            UploadingSessionData data = new UploadingSessionData(request.streamId(),
                    s3key, uploadId, request.expectedParts());

            uploadSessionService.saveIfNotExists(data);
            log.debug("UploadId not found in the database",
                    kv("streamId", request.streamId()),
                    kv("uploadId", uploadId),
                    kv("s3Key", s3key));
        }

        log.info("Multipart upload initiated",
                kv("streamId", request.streamId()),
                kv("filename", request.fileName()),
                kv("uploadId", uploadId));
        return new InitUploadingResponse(uploadId);
    }

    @Override
    public UploadPartsInfo getParts(String uploadId, Integer partNumberMarker) {
        if (uploadId == null || uploadId.isBlank()) {
            throw new IllegalArgumentException("uploadId is null or empty");
        }

        Optional<UploadSessionEntity> session = uploadSessionService.findByUploadId(uploadId);

        if (session.isEmpty()) {
            throw new IllegalArgumentException(String.format("uploadId not found: '%s'", uploadId));
        }

        String s3Key = session.get().getS3ObjectKey();
        Integer expectedParts = session.get().getExpectedParts();

        UploadPartsInfo uploadParts = storageRepository
                .getUploadPart(uploadId, s3Key, partNumberMarker, expectedParts);

        log.debug("Retrieved upload parts",
                kv("uploadId", uploadId),
                kv("s3Key", s3Key),
                kv("nextPartNumber", uploadParts.nextPartNumberMarker()));
        return uploadParts;
    }

    @Override
    public void completeUpload(CompleteUploadingRequest request) {
        log.debug("Completing multipart upload",
                kv("streamId", request.streamId()),
                kv("filename", request.fileName()));

        UploadSessionEntity session = uploadSessionService
                .findByStreamId(request.streamId())
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Upload session for '%s' not found", request.streamId())));

        String uploadId = session.getUploadId();
        int expectedParts =  session.getExpectedParts();
        String s3Key = buildS3ObjectKey(request.streamId(), request.fileName());

        storageRepository.completeUpload(uploadId, s3Key, expectedParts);

        log.info("Multipart upload completed",
                kv("streamId", request.streamId()),
                kv("uploadId", uploadId),
                kv("s3Key",s3Key));

        apiClient.notifyUploadCompleted(request.streamId());
    }

    @Override
    public RecordingDownloadsResponse generateDownloadUrls(RecordingDownloadRequest request) {
        List<RecordingDownloads> downloads = new ArrayList<>();
        int successfulDownloadUrls = 0;

        for (UUID streamId: request.streamIds()) {
            Optional<UploadSessionEntity> entity = uploadSessionService.findByStreamId(streamId);

            if (entity.isPresent()) {
                String s3Key = entity.get().getS3ObjectKey();
                String downloadUrl = storageRepository.generateDownloadUrl(s3Key);
                String filename = extractFileNameFromS3ObjectKey(s3Key);
                downloads.add(new RecordingDownloads(streamId, downloadUrl, filename));
                successfulDownloadUrls++;
            } else {
                log.warn("Entity not found in the database", kv("streamId", streamId));
            }
        }

        log.debug("Download URLs generated",
                kv("streamIdsCount", request.streamIds()),
                kv("successfulDownloadUrls", successfulDownloadUrls));

        return new RecordingDownloadsResponse(downloads);
    }


    private String buildS3ObjectKey(UUID streamId, String filename) {
        return String.format("%s/%s", streamId, filename);
    }

    private String extractFileNameFromS3ObjectKey(String objectKey) {
        return objectKey.substring(objectKey.indexOf('/') + 1, objectKey.indexOf('.'));
    }
}
