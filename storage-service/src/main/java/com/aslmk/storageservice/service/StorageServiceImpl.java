package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.*;
import com.aslmk.storageservice.repository.StorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;
    private final UploadSessionService uploadSessionService;

    public StorageServiceImpl(StorageRepository storageRepository,
                              UploadSessionService uploadSessionService) {
        this.storageRepository = storageRepository;
        this.uploadSessionService = uploadSessionService;
    }

    @Override
    public InitUploadingResponse initUpload(InitUploadingRequest request) {
        log.debug("Initiating multipart upload: streamId='{}', filename='{}'",
                request.streamId(), request.fileName());

        String s3key = buildS3ObjectKey(request.streamId(), request.fileName());

        String uploadId;
        Optional<UploadSessionEntity> session = uploadSessionService
                .findByS3ObjectPath(s3key);

        if (session.isPresent()) {
            uploadId = session.get().getUploadId();
            log.debug("Found uploadId in the database: uploadId='{}', s3Key='{}'", uploadId, s3key);
        } else {
            uploadId = storageRepository.generateUploadId(s3key);
            UploadingSessionData data = new UploadingSessionData(s3key, uploadId, request.expectedParts());
            uploadSessionService.saveIfNotExists(data);
            log.debug("uploadId not found in the database: uploadId='{}', s3Key='{}'",
                    uploadId, s3key);
        }

        log.info("Multipart upload initiated successfully: streamId='{}', filename='{}', uploadId='{}'",
                request.streamId(), request.fileName(), uploadId);
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

        String objectKey = session.get().getS3ObjectPath();
        Integer expectedParts = session.get().getExpectedParts();

        UploadPartsInfo uploadParts = storageRepository
                .getUploadPart(uploadId, objectKey, partNumberMarker, expectedParts);

        log.debug("Retrieving upload parts: uploadId='{}', s3Key='{}', nextPartNumber='{}'",
                uploadId, objectKey, uploadParts.nextPartNumberMarker());
        return uploadParts;
    }

    @Override
    public void completeUpload(CompleteUploadingRequest request) {
        log.debug("Completing multipart upload: streamId='{}', filename='{}'",
                request.streamId(), request.fileName());

        String s3Key = buildS3ObjectKey(request.streamId(), request.fileName());

        UploadSessionEntity session = uploadSessionService
                .findByS3ObjectPath(s3Key)
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Upload session for '%s' not found", s3Key)));

        String uploadId = session.getUploadId();
        int expectedParts =  session.getExpectedParts();
        storageRepository.completeUpload(uploadId, s3Key, expectedParts);

        log.info("Multipart upload completed: uploadId='{}', s3Key='{}'", uploadId, s3Key);
    }


    private String buildS3ObjectKey(UUID streamId, String filename) {
        return String.format("%s/%s", streamId, filename);
    }
}
