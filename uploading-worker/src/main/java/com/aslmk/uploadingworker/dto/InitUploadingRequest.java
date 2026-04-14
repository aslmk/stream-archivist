package com.aslmk.uploadingworker.dto;

public record InitUploadingRequest(String streamerUsername, String fileName, int expectedParts) {}
