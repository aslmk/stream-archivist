package com.aslmk.storageservice.service.impl;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.aslmk.storageservice.dto.PartUploadResultDto;
import com.aslmk.storageservice.dto.UploadCompletedEvent;
import com.aslmk.storageservice.dto.UploadingRequestDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;
import com.aslmk.storageservice.dto.InitMultipartUploadDto;
import com.aslmk.storageservice.repository.StorageRepository;
import com.aslmk.storageservice.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;

    public StorageServiceImpl(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public UploadingResponseDto initiateUpload(UploadingRequestDto request) {
        String s3Path = buildS3ObjectPath(request.getStreamerUsername(), request.getFileName());

        log.info("Initiating multipart upload: streamer={}, filename={}, s3Path={}",
                request.getStreamerUsername(), request.getFileName(), s3Path);

        InitMultipartUploadDto init = InitMultipartUploadDto.builder()
                .s3ObjectPath(s3Path)
                .fileParts(request.getFileParts())
                .build();

        return storageRepository.initiateUpload(init);
    }

    @Override
    public void completeUpload(UploadCompletedEvent uploadCompleted) {
        log.info("Completing multipart upload: uploadId={}, streamer={}, filename={}",
                uploadCompleted.getUploadId(),
                uploadCompleted.getStreamerUsername(),
                uploadCompleted.getFilename());

        List<PartETag> partETags = new ArrayList<>();

        for (PartUploadResultDto uploadResult: uploadCompleted.getPartUploadResults()) {
            PartETag partETag = new PartETag(uploadResult.getPartNumber(), uploadResult.getEtag());
            partETags.add(partETag);
        }

        log.debug("Collected {} part ETags for uploadId={}",
                partETags.size(), uploadCompleted.getUploadId());

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
