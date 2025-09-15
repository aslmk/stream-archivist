package com.aslmk.storageservice.service.impl;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;
import com.aslmk.storageservice.repository.StorageRepository;
import com.aslmk.storageservice.service.StorageService;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;

    public StorageServiceImpl(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public UploadingResponseDto initiateUpload(UploadingRequestDto request) {
        InitMultipartUploadDto init = InitMultipartUploadDto.builder()
                .s3ObjectPath(request.getStreamerUsername() + "/" + request.getFileName())
                .fileParts(request.getFileParts())
                .build();

        return storageRepository.initiateUpload(init);
    }
}
