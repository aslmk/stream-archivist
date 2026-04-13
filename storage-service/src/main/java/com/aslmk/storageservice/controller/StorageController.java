package com.aslmk.storageservice.controller;

import com.aslmk.storageservice.dto.UploadingRequestDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;
import com.aslmk.storageservice.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/uploads")
    public UploadingResponseDto processUpload(@RequestBody UploadingRequestDto request) {
        log.info("Processing upload: streamer: {}, filename: {}", request.getStreamerUsername(), request.getFileName());
        UploadingResponseDto response = storageService.processUpload(request);
        log.info("Upload processed successfully: uploadId={}", response.getUploadId());
        return response;
    }
}