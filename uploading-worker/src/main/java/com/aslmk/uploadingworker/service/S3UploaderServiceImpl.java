package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.dto.FilePartData;
import com.aslmk.uploadingworker.dto.HttpRangeInputStream;
import com.aslmk.uploadingworker.dto.PreSignedUrl;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.exception.FilePartUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Service
public class S3UploaderServiceImpl implements S3UploaderService {

    private final StorageServiceClient apiClient;

    public S3UploaderServiceImpl(StorageServiceClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void upload(S3UploadRequestDto request) {
        log.debug("Uploading file parts: file='{}', partsCount='{}', uploadUrlsCount='{}'",
                request.getFilePath(),
                request.getFileParts().size(),
                request.getUploadUrls().size());

        for (PreSignedUrl uploadUrl : request.getUploadUrls()) {
            int partNumber = uploadUrl.partNumber();
            String preSignedUrl = uploadUrl.url();
            FilePartData partData = getFilePartData(request, partNumber);
            File filePath = Path.of(request.getFilePath()).toFile();
            uploadPart(filePath, preSignedUrl, partData);
        }
    }

    private FilePartData getFilePartData(S3UploadRequestDto request, int partNumber) {
        Map<Integer, FilePartData> fileParts = request.getFileParts();
        if (fileParts.isEmpty()) throw new FilePartUploadException("File parts are empty");

        FilePartData partData = fileParts.get(partNumber);
        if (partData == null) {
            throw new FilePartUploadException(String.format(
                    "Failed to upload a file part: partData is null for partNumber='%d', file='%s'",
                    partNumber,
                    request.getFilePath()
            ));
        }
        return partData;
    }

    private void uploadPart(File file, String url, FilePartData partData) {
        try (HttpRangeInputStream hris = new HttpRangeInputStream(file,
                partData.offset(),
                partData.partSize())) {

            apiClient.uploadPart(url, hris, partData.partSize());

        } catch (IOException e) {
            throw new FilePartUploadException(
                    String.format("Failed to upload a file part: file='%s', cause='%s'",
                            file.getPath(),
                            e.getMessage())
            );
        }
    }
}
