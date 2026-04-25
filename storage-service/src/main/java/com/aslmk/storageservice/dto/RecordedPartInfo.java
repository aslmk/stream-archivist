package com.aslmk.storageservice.dto;

import java.util.UUID;

public record RecordedPartInfo(UUID streamId, Long partNumber, String filename) {}
