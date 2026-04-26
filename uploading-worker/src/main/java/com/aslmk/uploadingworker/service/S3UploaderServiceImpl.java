package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.exception.FilePartUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;

@Slf4j
@Service
public class S3UploaderServiceImpl implements S3UploaderService {

    private final StorageServiceClient apiClient;

    public S3UploaderServiceImpl(StorageServiceClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void upload(S3UploadRequestDto request) {
        try (RandomAccessFile raf = new RandomAccessFile(new File(request.getFilePath()), "r")) {

            for (PreSignedUrl uploadUrl : request.getUploadUrls()) {
                int partNumber = uploadUrl.partNumber();
                String preSignedUrl = uploadUrl.url();

                FilePartData partData = request.getFileParts().get(partNumber);

                raf.seek(partData.offset());
                byte[] bytes = new byte[(int) partData.partSize()];
                raf.readFully(bytes);

                apiClient.uploadPart(new S3Part(preSignedUrl, bytes));
            }
        } catch (Exception e) {
            throw new FilePartUploadException("Failed to upload part: " + e.getMessage());
        }
    }

    @Override
    public void uploadPart(UploadRecordedPart part) {
        try {
            long partSize = Files.size(part.filePath());
            apiClient.uploadPart(part.preSignedUrl().url(), part.filePath(), partSize);
        } catch (IOException e) {
            throw new FilePartUploadException("Failed to upload recorded part: " + e.getMessage());
        }
    }
}
