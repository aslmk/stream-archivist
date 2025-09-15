package com.aslmk.storageservice.service.impl;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.aslmk.common.dto.PartUploadResultDto;
import com.aslmk.common.dto.UploadCompletedEvent;
import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;
import com.aslmk.storageservice.repository.StorageRepository;
import com.aslmk.storageservice.service.StorageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;

    public StorageServiceImpl(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public UploadingResponseDto initiateUpload(UploadingRequestDto request) {
        InitMultipartUploadDto init = InitMultipartUploadDto.builder()
                .s3ObjectPath(buildS3ObjectPath(request.getStreamerUsername(), request.getFileName()))
                .fileParts(request.getFileParts())
                .build();

        return storageRepository.initiateUpload(init);
    }

    @Override
    public void completeUpload(UploadCompletedEvent uploadCompleted) {
        List<PartETag> partETags = new ArrayList<>();

        for (PartUploadResultDto uploadResult: uploadCompleted.getPartUploadResults()) {
            PartETag partETag = new PartETag(uploadResult.getPartNumber(), uploadResult.getEtag());
            partETags.add(partETag);
        }

        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
        request.setUploadId(uploadCompleted.getUploadId());
        request.setKey(buildS3ObjectPath(uploadCompleted.getStreamerUsername(), uploadCompleted.getFilename()));
        request.setPartETags(partETags);

        storageRepository.completeUpload(request);
    }

    private String buildS3ObjectPath(String streamerUsername, String filename) {
        return streamerUsername + "/" + filename;
    }
}
