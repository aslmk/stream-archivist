package com.aslmk.storageservice.dto;

import java.util.List;
import java.util.UUID;

public record RecordingDownloadRequest(List<UUID> streamIds) {}
