package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.config.RecordingStorageProperties;
import com.aslmk.uploadingworker.dto.*;
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
    public void processUploadingJob(UploadStreamRecordJob job) {
        validateJob(job);

        log.info("Starting upload to S3: streamId='{}', filename='{}'",
                job.getStreamId(), job.getFilename());

        try {
            Path filePath = getFilePath(job.getFilename());

            Map<Integer, FilePartData> fileParts = fileSplitter.getFileParts(filePath);
            int filePartsCount = fileParts.size();
            log.debug("File split: file='{}', part(s)='{}'", job.getFilename(), filePartsCount);

            String uploadId = initUpload(job, filePartsCount);
            uploadParts(uploadId, filePath, fileParts);

            try {
                completeUpload(job);
            } catch (StorageServiceException e) {
                log.warn("Error occured while completing upload: uploadId='{}', filename='{}', error='{}'. Trying again",
                        uploadId, job.getFilename(), e.getMessage());
                uploadParts(uploadId, filePath, fileParts);
                completeUpload(job);
            }

            log.info("Upload completed successfully: streamId='{}', filename='{}'",
                    job.getStreamId(), job.getFilename());
        } catch (RuntimeException e) {
            throw new StreamUploadException(String
                    .format("Failed to upload file: filename='%s'", job.getFilename()), e);
        }
    }

    private void validateJob(UploadStreamRecordJob job) {
        if (job.getFilename() == null || job.getFilename().isBlank()) {
            throw new StreamUploadException("Failed to process uploading job: filename is missing");
        }

        if (job.getStreamId() == null) {
            throw new StreamUploadException("Failed to process uploading job: streamId is missing");
        }
    }

    private Path getFilePath(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.error("Processing failed: filename is missing");
            throw new StreamUploadException("Failed to process uploading job: File name is required");
        }

        try {
            Path filePath = getStoragePath().resolve(fileName);

            log.debug("Resolved file path: {}", filePath);
            return filePath;
        } catch (InvalidPathException e) {
            log.error("Invalid file path for '{}'", fileName);
            throw new StreamUploadException("Failed to process uploading job: Invalid path", e);
        }
    }

    private Path getStoragePath() {
        return Paths.get(properties.getPath()).toAbsolutePath().normalize();
    }

    private void uploadParts(String uploadId, Path filePath, Map<Integer, FilePartData> fileParts) {
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
        } while (hasNext && nextPartNumberMarker != null);
    }

    private String initUpload(UploadStreamRecordJob job, int filePartsCount) {
        InitUploadingRequest initRequest = new InitUploadingRequest(job.getStreamId(),
                job.getFilename(), filePartsCount);
        InitUploadingResponse initResponse = apiClient.initUpload(initRequest);
        return initResponse.uploadId();
    }

    private void completeUpload(UploadStreamRecordJob job) {
        CompleteUploadingRequest completeRequest = new CompleteUploadingRequest(job.getStreamId(),
                job.getFilename());
        apiClient.compelteUpload(completeRequest);
    }
}
