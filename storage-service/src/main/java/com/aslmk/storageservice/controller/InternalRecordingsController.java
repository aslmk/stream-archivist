package com.aslmk.storageservice.controller;

import com.aslmk.storageservice.dto.RecordingDownloadRequest;
import com.aslmk.storageservice.dto.RecordingDownloadsResponse;
import com.aslmk.storageservice.service.StorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/recordings")
public class InternalRecordingsController {

    private final StorageService service;

    public InternalRecordingsController(StorageService service) {
        this.service = service;
    }

    @PostMapping("/downloads")
    public RecordingDownloadsResponse generateDownloadUrls(@RequestBody RecordingDownloadRequest request) {
        return service.generateDownloadUrls(request);
    }
}
