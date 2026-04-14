package com.aslmk.storageservice.dto;

public record UploadingSessionData(String objectKey, String uploadId, int expectedParts) {}
