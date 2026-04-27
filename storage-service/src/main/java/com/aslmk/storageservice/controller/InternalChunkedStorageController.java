package com.aslmk.storageservice.controller;

import com.aslmk.storageservice.dto.CompleteChunkedUpload;
import com.aslmk.storageservice.dto.InitChunkedUpload;
import com.aslmk.storageservice.dto.PreSignedUrl;
import com.aslmk.storageservice.dto.RecordedPartInfo;
import com.aslmk.storageservice.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Deprecated(forRemoval = true)
@RestController
@RequestMapping("/internal/storage/chunked-uploads")
public class InternalChunkedStorageController {
    private final StorageService storageService;

    public InternalChunkedStorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/init")
    @ResponseStatus(HttpStatus.CREATED)
    public void initChunkedUpload(@RequestBody InitChunkedUpload init) {
        storageService.initChunkedUpload(init);
    }

    @GetMapping("/signed-url")
    public PreSignedUrl getPreSignedUrl(@RequestParam(name = "streamId") String streamId,
                                        @RequestParam(name = "partNumber") Long partNumber,
                                        @RequestParam(name = "filename") String filename) {
        RecordedPartInfo info = new RecordedPartInfo(UUID.fromString(streamId), partNumber, filename);
        return storageService.getPreSignedUrl(info);
    }

    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeChunkedUpload(@RequestBody CompleteChunkedUpload complete) {
        storageService.completeChunkedUpload(complete);
    }
}
