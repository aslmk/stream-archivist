package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.config.RecordingStorageProperties;
import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.exception.StorageServiceException;
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
    private final FileSplitterService fileSplitterService;
    private final StorageServiceClient storageServiceClient;
    private final S3UploaderService uploaderService;

    public StreamUploaderServiceImpl(RecordingStorageProperties properties,
                                     FileSplitterService fileSplitterService,
                                     StorageServiceClient storageServiceClient,
                                     S3UploaderService uploaderService) {
        this.properties = properties;
        this.fileSplitterService = fileSplitterService;
        this.storageServiceClient = storageServiceClient;
        this.uploaderService = uploaderService;
    }

    @Override
    public void processUploadingRequest(RecordingStatusEvent event) {
        if (event.getStreamerUsername() == null ||
                event.getStreamerUsername().isBlank()) {
            log.error("Processing failed: streamerUsername is missing");
            throw new StreamUploadException("Failed to process uploading request: streamerUsername is required");
        }

        log.info("Start processing uploading request: streamer='{}', filename='{}'",
                event.getStreamerUsername(),
                event.getFilename()
        );

        String uploadId;
        boolean hasNext;
        Integer nextPartNumberMarker = 0;
        try {
            do {
                log.debug("Resolving file path for '{}'", event.getFilename());
                Path filePath = getFilePath(event.getFilename());

                log.info("Splitting file into parts: {}", filePath);
                Map<Integer, FilePartData> fileParts = fileSplitterService.getFileParts(filePath);
                log.debug("File split into {} part(s)", fileParts.size());

                UploadingRequestDto request = UploadingRequestDto.builder()
                        .streamerUsername(event.getStreamerUsername())
                        .fileParts(fileParts.size())
                        .fileName(event.getFilename())
                        .nextPartNumberMarker(nextPartNumberMarker)
                        .build();

                log.info("Sending process upload request to storage-service");
                UploadingResponseDto response = storageServiceClient.processUpload(request);
                uploadId = response.getUploadId();
                log.debug("Received processUpload response: uploadId='{}'", uploadId);

                S3UploadRequestDto s3UploadRequest = S3UploadRequestDto.builder()
                        .uploadUrls(response.getUploadUrls())
                        .filePath(filePath.toString())
                        .fileParts(fileParts)
                        .build();

                log.info("Uploading {} parts to S3", fileParts.size());
                uploaderService.upload(s3UploadRequest);
                log.debug("Successfully uploaded all parts for '{}'", event.getFilename());

                hasNext = response.isHasNext();
                nextPartNumberMarker = response.getNextPartNumberMarker();
            } while (hasNext);

            log.info("Upload processing completed successfully: streamer='{}', filename='{}'",
                    event.getStreamerUsername(),
                    event.getFilename());

        } catch (Exception e) {
            log.error("Error while processing uploading request: streamer='{}', filename='{}'",
                    event.getStreamerUsername(),
                    event.getFilename(),
                    e);

            switch (e) {
                case FileSplittingException fse ->
                        throw new StreamUploadException(fse.getMessage(), e);
                case StorageServiceException sse ->
                        throw new StreamUploadException(sse.getMessage(), e);
                case FileChunkUploadException fue ->
                        throw new StreamUploadException(fue.getMessage(), e);
                default ->
                        throw new StreamUploadException("Failed to process uploading request", e);
            }
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
