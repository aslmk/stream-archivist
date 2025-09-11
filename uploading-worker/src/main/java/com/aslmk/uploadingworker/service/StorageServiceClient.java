package com.aslmk.uploadingworker.service;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StorageServiceClient {

    @Value("${user.api.storage-service-base-url}")
    private String storageServiceBaseUrl;
    @Value("${user.api.storage-service-upload-init-endpoint}")
    private String uploadInitEndpoint;

    private final RestClient restClient;

    public StorageServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public UploadingResponseDto uploadInit(UploadingRequestDto request) {
        return restClient.post()
                .uri(storageServiceBaseUrl + uploadInitEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(UploadingResponseDto.class)
                .getBody();
    }
}
