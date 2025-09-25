package com.aslmk.uploadingworker.service.impl;

import com.aslmk.common.dto.*;
import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import com.aslmk.uploadingworker.exception.StreamUploadException;
import com.aslmk.uploadingworker.kafka.producer.KafkaService;
import com.aslmk.uploadingworker.service.FileSplitterService;
import com.aslmk.uploadingworker.service.S3UploaderService;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import com.aslmk.uploadingworker.service.StreamUploaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class StreamUploaderServiceImpl implements StreamUploaderService {
    @Value("${user.file.save-directory}")
    private String saveDirectory;
    private static final String RECORDINGS_DIR = "recordings";

    private final FileSplitterService fileSplitterService;
    private final StorageServiceClient storageServiceClient;
    private final S3UploaderService uploaderService;
    private final KafkaService kafkaService;


    public StreamUploaderServiceImpl(FileSplitterService fileSplitterService, StorageServiceClient storageServiceClient, S3UploaderService uploaderService, KafkaService kafkaService) {
        this.fileSplitterService = fileSplitterService;
        this.storageServiceClient = storageServiceClient;
        this.uploaderService = uploaderService;
        this.kafkaService = kafkaService;
    }

    @Override
    public void processUploadingRequest(RecordCompletedEvent recordCompletedEvent) {
        if (recordCompletedEvent.getStreamerUsername() == null ||
                recordCompletedEvent.getStreamerUsername().isBlank()) {
            throw new StreamUploadException("Failed to process uploading request: streamerUsername is required");
        }

        try {
            Path filePath = getFilePath(recordCompletedEvent.getFileName());
            List<FilePart> fileParts = fileSplitterService.getFileParts(filePath);

            UploadingRequestDto request = UploadingRequestDto.builder()
                    .streamerUsername(recordCompletedEvent.getStreamerUsername())
                    .fileParts(fileParts.size())
                    .fileName(recordCompletedEvent.getFileName())
                    .build();


            UploadingResponseDto response = storageServiceClient.uploadInit(request);

            S3UploadRequestDto s3UploadRequest = S3UploadRequestDto.builder()
                    .uploadUrls(response.getUploadURLs())
                    .filePath(filePath.toString())
                    .fileParts(fileParts)
                    .build();

            List<PartUploadResultDto> partUploadResults = uploaderService.upload(s3UploadRequest);

            UploadCompletedEvent uploadCompletedEvent = UploadCompletedEvent.builder()
                    .partUploadResults(partUploadResults)
                    .filename(recordCompletedEvent.getFileName())
                    .streamerUsername(recordCompletedEvent.getStreamerUsername())
                    .uploadId(response.getUploadId())
                    .build();

            kafkaService.send(uploadCompletedEvent);
        } catch (Exception e) {
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
            throw new StreamUploadException("Failed to process uploading request: File name is required");
        }

        Path fullFilePath;
        try {
            Path currentDir = Paths.get("").toAbsolutePath();
            Path projectRoot = currentDir.getParent();
            String filePath = projectRoot.resolve(saveDirectory).resolve(RECORDINGS_DIR).toString();
            fullFilePath = Paths.get(filePath + "/" + fileName);
        } catch (InvalidPathException e) {
            throw new StreamUploadException("Failed to process uploading request: Invalid path", e);
        }

        return fullFilePath;
    }
}
