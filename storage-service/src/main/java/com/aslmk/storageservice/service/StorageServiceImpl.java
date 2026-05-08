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
        String s3key = buildS3ObjectKey(request.streamId(), request.fileName());

        String uploadId;
        Optional<UploadSessionEntity> session = uploadSessionService
                .findByS3ObjectPath(s3key);

        if (session.isPresent()) {
            uploadId = session.get().getUploadId();
        } else {
            uploadId = storageRepository.generateUploadId(s3key);
            UploadingSessionData data = new UploadingSessionData(s3key, uploadId, request.expectedParts());
            uploadSessionService.saveIfNotExists(data);
        }

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

        return storageRepository.getUploadPart(uploadId, objectKey, partNumberMarker, expectedParts);
    }

    @Override
    public void completeUpload(CompleteUploadingRequest request) {
        String s3Key = buildS3ObjectKey(request.streamId(), request.fileName());

        UploadSessionEntity session = uploadSessionService
                .findByS3ObjectPath(s3Key)
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Upload session for '%s' not found", s3Key)));

        String uploadId = session.getUploadId();
        int expectedParts =  session.getExpectedParts();
        storageRepository.completeUpload(uploadId, s3Key, expectedParts);
    }


    private String buildS3ObjectKey(UUID streamId, String filename) {
        return String.format("%s/%s", streamId, filename);
    }
}
