package com.aslmk.storageservice.service;

import com.aslmk.storageservice.domain.StreamSessionEntity;
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
    private final StreamSessionService streamSessionService;

    public StorageServiceImpl(StorageRepository storageRepository,
                              UploadSessionService uploadSessionService,
                              StreamSessionService streamSessionService) {
        this.storageRepository = storageRepository;
        this.uploadSessionService = uploadSessionService;
        this.streamSessionService = streamSessionService;
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


    @Override
    public void initChunkedUpload(InitChunkedUpload init) {
        log.info("Initializing multipart upload for chunked recording mode: streamId='{}'",
                init.streamId());

        String s3Key = String.format("%s/%s", init.streamId(), init.filename());
        Optional<StreamSessionEntity> session = streamSessionService.getByStreamId(init.streamId());

        if (session.isEmpty()) {
            String uploadId = storageRepository.generateUploadId(s3Key);
            StreamSessionData data = new StreamSessionData(init.streamId(), uploadId, s3Key);
            streamSessionService.saveIfNotExists(data);
        }
    }

    @Override
    public PreSignedUrl getPreSignedUrl(RecordedPartInfo part) {
        String s3Key = String.format("%s/%s", part.streamId(), part.filename());
        String uploadId = streamSessionService.getUploadId(part.streamId());

        log.debug("Generating pre-signed URL: streamId='{}', partNumber='{}', s3Key='{}', uploadId='{}'",
                part.streamId(), part.partNumber(), s3Key, uploadId);
        return storageRepository.generatePreSignedUrl(uploadId, part.partNumber(), s3Key);
    }

    @Override
    public void completeChunkedUpload(CompleteChunkedUpload complete) {
        log.info("Completing multipart upload for chunked recording mode: streamId='{}'",
                complete.streamId());

        String s3Key = String.format("%s/%s", complete.streamId(), complete.filename());
        String uploadId = streamSessionService.getUploadId(complete.streamId());

        storageRepository.completeChunkedUpload(uploadId, s3Key);
        streamSessionService.removeByStreamId(complete.streamId());
    }

    private String buildS3ObjectPath(String streamerUsername, String filename) {
        return streamerUsername + "/" + filename;
    }
}
