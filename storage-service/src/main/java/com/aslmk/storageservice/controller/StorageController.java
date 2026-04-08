package com.aslmk.storageservice.controller;

import com.aslmk.storageservice.dto.UploadingRequestDto;
import com.aslmk.storageservice.dto.UploadingResponseDto;
import com.aslmk.storageservice.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/uploads")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadingResponseDto createUpload(@RequestBody UploadingRequestDto request) {
        log.info("Initiating upload: streamer: {}, filename: {}", request.getStreamerUsername(), request.getFileName());
        UploadingResponseDto response = storageService.initiateUpload(request);
        log.info("Upload initiated successfully: uploadId={}", response.getUploadId());
        return response;
    }
}