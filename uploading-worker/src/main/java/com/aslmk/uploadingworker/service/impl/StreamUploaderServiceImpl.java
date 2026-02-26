package com.aslmk.uploadingworker.service.impl;

import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.config.RecordingStorageProperties;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import com.aslmk.uploadingworker.exception.StreamUploadException;
import com.aslmk.uploadingworker.kafka.producer.KafkaService;
import com.aslmk.uploadingworker.service.FileSplitterService;
import com.aslmk.uploadingworker.service.S3UploaderService;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import com.aslmk.uploadingworker.service.StreamUploaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class StreamUploaderServiceImpl implements StreamUploaderService {

    private final RecordingStorageProperties properties;
    private final FileSplitterService fileSplitterService;
    private final StorageServiceClient storageServiceClient;
    private final S3UploaderService uploaderService;
    private final KafkaService kafkaService;


    public StreamUploaderServiceImpl(RecordingStorageProperties properties, FileSplitterService fileSplitterService, StorageServiceClient storageServiceClient, S3UploaderService uploaderService, KafkaService kafkaService) {
        this.properties = properties;
        this.fileSplitterService = fileSplitterService;
        this.storageServiceClient = storageServiceClient;
        this.uploaderService = uploaderService;
        this.kafkaService = kafkaService;
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

        try {
            log.debug("Resolving file path for '{}'", event.getFilename());
            Path filePath = getFilePath(event.getFilename());

            log.info("Splitting file into parts: {}", filePath);
            List<FilePart> fileParts = fileSplitterService.getFileParts(filePath);
            log.debug("File split into {} part(s)", fileParts.size());

            UploadingRequestDto request = UploadingRequestDto.builder()
                    .streamerUsername(event.getStreamerUsername())
                    .fileParts(fileParts.size())
                    .fileName(event.getFilename())
                    .build();

            log.info("Sending uploadInit request to storage-service");
            UploadingResponseDto response = storageServiceClient.uploadInit(request);
            log.debug("Received uploadInit response: uploadId='{}'", response.getUploadId());

            S3UploadRequestDto s3UploadRequest = S3UploadRequestDto.builder()
                    .uploadUrls(response.getUploadURLs())
                    .filePath(filePath.toString())
                    .fileParts(fileParts)
                    .build();

            log.info("Uploading {} parts to S3", fileParts.size());
            List<PartUploadResultDto> partUploadResults = uploaderService.upload(s3UploadRequest);
            log.debug("Successfully uploaded all parts for '{}'", event.getFilename());


            UploadCompletedEvent uploadCompletedEvent = UploadCompletedEvent.builder()
                    .partUploadResults(partUploadResults)
                    .filename(event.getFilename())
                    .streamerUsername(event.getStreamerUsername())
                    .uploadId(response.getUploadId())
                    .build();

            log.info("Sending UploadCompletedEvent to Kafka");
            kafkaService.send(uploadCompletedEvent);

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
