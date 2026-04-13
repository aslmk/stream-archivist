package com.aslmk.uploadingworker.service;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.dto.FilePartData;
import com.aslmk.uploadingworker.dto.PartUploadResultDto;
import com.aslmk.uploadingworker.dto.S3PartDto;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        try (RandomAccessFile raf = new RandomAccessFile(new File(request.getFilePath()), "r")) {
            List<PartUploadResultDto> uploadResults = new ArrayList<>();

            // TODO: replace Map<> with record PreSignedUrl(partNumber, url)
            for (Map.Entry<Integer, String> entry: request.getUploadUrls().entrySet()) {
                int partNumber = entry.getKey();
                String uploadUrl = entry.getValue();
                FilePartData partData = request.getFileParts().get(partNumber);

                raf.seek(partData.offset());
                byte[] bytes = new byte[(int) partData.partSize()];
                raf.readFully(bytes);

                S3PartDto s3Part = S3PartDto.builder()
                        .preSignedUrl(uploadUrl)
                        .partData(bytes)
                        .build();

                String etag = storageServiceClient.uploadChunk(s3Part);
                uploadResults.add(new PartUploadResultDto(partNumber, etag));
            }

            log.info("All parts uploaded successfully: total={}", uploadResults.size());
            return uploadResults;
        } catch (Exception e) {
            log.error("Error during S3 multipart upload for filePath='{}': {}",
                    request.getFilePath(), e.getMessage(), e);
            throw new FileChunkUploadException("Error while uploading chunk to S3: " + e.getMessage());
        }
    }
}
