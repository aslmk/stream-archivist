package com.aslmk.storageservice.dto;

public record InitUploadingRequest(String streamerUsername, String fileName, int expectedParts) {}
