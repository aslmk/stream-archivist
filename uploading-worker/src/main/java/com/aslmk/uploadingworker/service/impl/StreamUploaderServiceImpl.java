package com.aslmk.uploadingworker.service.impl;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.dto.PartUploadResultDto;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.service.FileSplitterService;
import com.aslmk.uploadingworker.service.S3UploaderService;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import com.aslmk.uploadingworker.service.StreamUploaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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


    public StreamUploaderServiceImpl(FileSplitterService fileSplitterService, StorageServiceClient storageServiceClient, S3UploaderService uploaderService) {
        this.fileSplitterService = fileSplitterService;
        this.storageServiceClient = storageServiceClient;
        this.uploaderService = uploaderService;
    }

    @Override
    public void processUploadingRequest(String streamerUsername, String fileName) {
        Path filePath = getFilePath(fileName);
        List<FilePart> fileParts = fileSplitterService.getFileParts(filePath);

        UploadingRequestDto request = UploadingRequestDto.builder()
                .streamerUsername(streamerUsername)
                .fileParts(fileParts.size())
                .fileName(fileName)
                .build();

        UploadingResponseDto response = storageServiceClient.uploadInit(request);

        S3UploadRequestDto s3UploadRequest = S3UploadRequestDto.builder()
                .uploadUrls(response.getUploadURLs())
                .uploadId(response.getUploadId())
                .filePath(filePath.toString())
                .fileParts(fileParts)
                .build();

        List<PartUploadResultDto> partUploadResults = uploaderService.upload(s3UploadRequest);

        // TODO: send partUploadResults to Kafka producer (next task)
    }

    private Path getFilePath(String fileName) {
        Path currentDir = Paths.get("").toAbsolutePath();
        Path projectRoot = currentDir.getParent();
        String filePath = projectRoot.resolve(saveDirectory).resolve(RECORDINGS_DIR).toString();
        return Paths.get(filePath + "/" + fileName);
    }
}
