package com.aslmk.uploadingworker.service.impl;

import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.dto.S3PartDto;
import com.aslmk.common.dto.PartUploadResultDto;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import com.aslmk.uploadingworker.service.S3UploaderService;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class S3UploaderServiceImpl implements S3UploaderService {

    private final StorageServiceClient storageServiceClient;

    public S3UploaderServiceImpl(StorageServiceClient storageServiceClient) {
        this.storageServiceClient = storageServiceClient;
    }

    @Override
    public List<PartUploadResultDto> upload(S3UploadRequestDto request) {

        if (request.getFileParts().size() != request.getUploadUrls().size()) {
            throw new FileChunkUploadException("Number of file parts and upload urls do not match");
        }

        List<PartUploadResultDto> uploadResults = new ArrayList<>();

        File file = new File(request.getFilePath());

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (FilePart part : request.getFileParts()) {
                log.info("Starting to upload partNumber={}", part.partNumber());

                raf.seek(part.offset());

                byte[] bytes = new byte[(int) part.partSize()];
                raf.readFully(bytes);

                String uploadUrl = request.getUploadUrls().get((int) (part.partNumber()-1));
                log.info("Upload URL={}", uploadUrl);

                S3PartDto s3Part = S3PartDto.builder()
                        .preSignedUrl(uploadUrl)
                        .partData(bytes)
                        .build();

                String etag = storageServiceClient.uploadChunk(s3Part);

                uploadResults.add(new PartUploadResultDto((int) part.partNumber(), etag));
            }
        } catch (Exception e) {
            throw new FileChunkUploadException("Error while uploading chunk to S3: " + e.getMessage());
        }

        return uploadResults;
    }
}
