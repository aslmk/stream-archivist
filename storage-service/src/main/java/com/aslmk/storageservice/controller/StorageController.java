package com.aslmk.storageservice.controller;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.storageservice.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UploadingResponseDto> createUpload(@RequestBody UploadingRequestDto request) {
        log.info("Initiating upload: streamer: {}, filename: {}", request.getStreamerUsername(), request.getFileName());
        UploadingResponseDto response = storageService.initiateUpload(request);
        log.info("Upload initiated successfully: uploadId={}", response.getUploadId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}