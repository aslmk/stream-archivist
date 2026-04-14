package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.UploadSessionEntity;
import com.aslmk.storageservice.dto.*;
import com.aslmk.storageservice.repository.StorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        String s3Path = buildS3ObjectPath(request.streamerUsername(), request.fileName());

        String uploadId;
        Optional<UploadSessionEntity> session = uploadSessionService
                .findByS3ObjectPath(s3Path);

        if (session.isPresent()) {
            uploadId = session.get().getUploadId();
        } else {
            uploadId = storageRepository.generateUploadId(s3Path);
            UploadingSessionData data = new UploadingSessionData(s3Path, uploadId, request.expectedParts());
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

    private String buildS3ObjectPath(String streamerUsername, String filename) {
        return streamerUsername + "/" + filename;
    }
}
