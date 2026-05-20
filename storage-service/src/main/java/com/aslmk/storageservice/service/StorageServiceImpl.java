package com.aslmk.storageservice.service;

import com.aslmk.storageservice.client.RecordingOrchestratorClient;
import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.*;
import com.aslmk.storageservice.repository.StorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                .findByS3ObjectPath(s3key);

        if (session.isPresent()) {
            uploadId = session.get().getUploadId();
            log.debug("Found uploadId in the database",
                    kv("uploadId", uploadId),
                    kv("s3Key", s3key));
        } else {
            uploadId = storageRepository.generateUploadId(s3key);
            UploadingSessionData data = new UploadingSessionData(s3key, uploadId, request.expectedParts());
            uploadSessionService.saveIfNotExists(data);
            log.debug("UploadId not found in the database",
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

        String s3Key = session.get().getS3ObjectPath();
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

        String s3Key = buildS3ObjectKey(request.streamId(), request.fileName());

        UploadSessionEntity session = uploadSessionService
                .findByS3ObjectPath(s3Key)
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Upload session for '%s' not found", s3Key)));

        String uploadId = session.getUploadId();
        int expectedParts =  session.getExpectedParts();
        storageRepository.completeUpload(uploadId, s3Key, expectedParts);

        log.info("Multipart upload completed",
                kv("streamId", request.streamId()),
                kv("uploadId", uploadId),
                kv("s3Key",s3Key));

        apiClient.notifyUploadCompleted(request.streamId());
    }


    private String buildS3ObjectKey(UUID streamId, String filename) {
        return String.format("%s/%s", streamId, filename);
    }
}
