package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.config.RecordingStorageProperties;
import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.exception.StreamUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Service
public class StreamUploaderServiceImpl implements StreamUploaderService {

    private final RecordingStorageProperties properties;
    private final FileSplitterService fileSplitter;
    private final StorageServiceClient apiClient;
    private final S3UploaderService uploader;

    public StreamUploaderServiceImpl(RecordingStorageProperties properties,
                                     FileSplitterService fileSplitter,
                                     StorageServiceClient apiClient,
                                     S3UploaderService uploader) {
        this.properties = properties;
        this.fileSplitter = fileSplitter;
        this.apiClient = apiClient;
        this.uploader = uploader;
    }

    @Override
    public void processUploadingRequest(RecordingStatusEvent event) {
        validateEvent(event);

        if (event.getEventType().equals(RecordingEventType.RECORDING_STARTED)) {
            return;
        }

        log.info("Start uploading to S3: streamer='{}', filename='{}'",
                event.getStreamerUsername(), event.getFilename());

        try {
            Path filePath = getFilePath(event.getFilename());
            Map<Integer, FilePartData> fileParts = fileSplitter.getFileParts(filePath);
            int filePartsCount = fileParts.size();
            log.debug("File ('{}') split: '{}' part(s)", event.getFilename(), filePartsCount);

            InitUploadingRequest initRequest = new InitUploadingRequest(event.getStreamerUsername(),
                    event.getFilename(), filePartsCount);
            InitUploadingResponse initResponse = apiClient.initUpload(initRequest);
            String uploadId = initResponse.uploadId();

            boolean hasNext;
            Integer nextPartNumberMarker = 0;
            do {
                UploadPartsInfo partsInfo = apiClient.getUploadParts(uploadId, nextPartNumberMarker);

                S3UploadRequestDto dto = S3UploadRequestDto.builder()
                        .uploadUrls(partsInfo.uploadUrls())
                        .filePath(filePath.toString())
                        .fileParts(fileParts)
                        .build();

                uploader.upload(dto);

                hasNext = partsInfo.hasNext();
                nextPartNumberMarker = partsInfo.nextPartNumberMarker();
            } while (hasNext);

            apiClient.getUploadParts(uploadId, nextPartNumberMarker);

            log.info("Upload completed successfully: streamer='{}', filename='{}'",
                    event.getStreamerUsername(), event.getFilename());
        } catch (Exception e) {
            throw new StreamUploadException(String
                    .format("Failed to upload file: filename='%s'", event.getFilename()), e);
        }

    }

    private void validateEvent(RecordingStatusEvent event) {
        if (event.getStreamerUsername() == null || event.getStreamerUsername().isBlank()) {
            throw new StreamUploadException("Failed to process uploading request: streamerUsername is missing");
        }
        if (event.getFilename() == null || event.getFilename().isBlank()) {
            throw new StreamUploadException("Failed to process uploading request: filename is missing");
        }

        if (event.getStreamId() == null) {
            throw new StreamUploadException("Failed to process uploading request: streamId is missing");
        }
    }

    private Path getFilePath(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.error("Processing failed: filename is missing");
            throw new StreamUploadException("Failed to process uploading request: File name is required");
        }

        try {
            Path filePath = getStoragePath().resolve(fileName);

            log.debug("Resolved file path: {}", filePath);
            return filePath;
        } catch (InvalidPathException e) {
            log.error("Invalid file path for '{}'", fileName);
            throw new StreamUploadException("Failed to process uploading request: Invalid path", e);
        }
    }

    private Path getStoragePath() {
        return Paths.get(properties.getPath()).toAbsolutePath().normalize();
    }
}
