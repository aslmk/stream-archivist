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
        log.info("Starting S3 multipart upload for filePath='{}'", request.getFilePath());

        if (request.getFileParts().size() != request.getUploadUrls().size()) {
            log.error("Number of file parts ({}) does not match number of presigned URLs ({})",
                    request.getFileParts().size(),
                    request.getUploadUrls().size());
            throw new FileChunkUploadException("Number of file parts and upload urls do not match");
        }

        List<PartUploadResultDto> uploadResults = new ArrayList<>();

        File file = new File(request.getFilePath());

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (FilePart part : request.getFileParts()) {

                log.info("Uploading part #{} (offset={}, size={})",
                        part.partNumber(), part.offset(), part.partSize());

                raf.seek(part.offset());

                byte[] bytes = new byte[(int) part.partSize()];
                raf.readFully(bytes);

                String uploadUrl = request.getUploadUrls().get((int) (part.partNumber()-1));
                log.debug("Using presigned URL for part #{}: {}", part.partNumber(), uploadUrl);

                S3PartDto s3Part = S3PartDto.builder()
                        .preSignedUrl(uploadUrl)
                        .partData(bytes)
                        .build();

                String etag = storageServiceClient.uploadChunk(s3Part);

                log.info("Successfully uploaded part #{} (ETag={})", part.partNumber(), etag);

                uploadResults.add(new PartUploadResultDto((int) part.partNumber(), etag));
            }
        } catch (Exception e) {
            log.error("Error during S3 multipart upload for filePath='{}': {}", request.getFilePath(), e.getMessage(), e);
            throw new FileChunkUploadException("Error while uploading chunk to S3: " + e.getMessage());
        }

        log.info("All parts uploaded successfully: total={}", uploadResults.size());

        return uploadResults;
    }
}
