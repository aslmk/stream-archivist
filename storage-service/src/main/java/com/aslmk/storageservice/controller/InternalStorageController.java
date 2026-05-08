package com.aslmk.storageservice.controller;

import com.aslmk.storageservice.dto.CompleteUploadingRequest;
import com.aslmk.storageservice.dto.InitUploadingRequest;
import com.aslmk.storageservice.dto.InitUploadingResponse;
import com.aslmk.storageservice.dto.UploadPartsInfo;
import com.aslmk.storageservice.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/storage/uploads")
public class InternalStorageController {
    private final StorageService storageService;

    public InternalStorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InitUploadingResponse initUpload(@RequestBody InitUploadingRequest request) {
        return storageService.initUpload(request);
    }

    @GetMapping("/{uploadId}/parts")
    public UploadPartsInfo getParts(@PathVariable(value = "uploadId") String uploadId,
                                    @RequestParam(value = "partNumberMarker") Integer partNumberMarker) {
        return storageService.getParts(uploadId, partNumberMarker);
    }

    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeUpload(@RequestBody CompleteUploadingRequest request) {
        storageService.completeUpload(request);
    }
}
