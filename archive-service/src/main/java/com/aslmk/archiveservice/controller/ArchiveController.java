package com.aslmk.archiveservice.controller;

import com.aslmk.archiveservice.dto.StreamRecordings;
import com.aslmk.archiveservice.service.ArchiveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/archive")
public class ArchiveController {
    private final ArchiveService service;

    public ArchiveController(ArchiveService service) {
        this.service = service;
    }

    @GetMapping("/{streamerId}")
    public StreamRecordings getStreamRecordings(@PathVariable UUID streamerId) {
        return service.getStreamRecordings(streamerId);
    }
}
