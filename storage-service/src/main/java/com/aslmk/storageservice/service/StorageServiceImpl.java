package com.aslmk.storageservice.service;

import com.aslmk.storageservice.dto.MultipartUploadDto;
import com.aslmk.storageservice.dto.UploadingRequestDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;
import com.aslmk.storageservice.repository.StorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;

    public StorageServiceImpl(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public UploadingResponseDto processUpload(UploadingRequestDto request) {
        String s3Path = buildS3ObjectPath(request.getStreamerUsername(), request.getFileName());

        log.info("Processing multipart upload: streamer={}, filename={}, s3Path={}",
                request.getStreamerUsername(), request.getFileName(), s3Path);

        MultipartUploadDto init = MultipartUploadDto.builder()
                .s3ObjectPath(s3Path)
                .fileParts(request.getFileParts())
                .nextPartNumberMarker(request.getNextPartNumberMarker())
                .build();

        return storageRepository.processUpload(init);
    }

    private String buildS3ObjectPath(String streamerUsername, String filename) {
        return streamerUsername + "/" + filename;
    }
}
