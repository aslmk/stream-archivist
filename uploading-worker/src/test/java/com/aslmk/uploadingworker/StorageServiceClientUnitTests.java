package com.aslmk.uploadingworker;

import com.aslmk.common.dto.UploadingRequestDto;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.uploadingworker.dto.S3PartDto;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class StorageServiceClientUnitTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @InjectMocks
    private StorageServiceClient client;

    private UploadingResponseDto testResponse;
    private UploadingRequestDto request;
    private S3PartDto s3Part;

    private MultiValueMap<String, String> headers;
    private static final String TEST_ETAG_HEADER_KEY = "ETag";
    private static final String TEST_ETAG_HEADER_VALUE = "0fh20hf320foe3h229hf41fos41";

    @BeforeEach
    public void setUp() {
        headers = new LinkedMultiValueMap<>();
        headers.add(TEST_ETAG_HEADER_KEY, TEST_ETAG_HEADER_VALUE);

         testResponse = UploadingResponseDto.builder()
                .uploadId("9h9b9b9")
                .uploadURLs(List.of("https://test-url"))
                .build();

        s3Part = S3PartDto.builder()
                .partData(new byte[100])
                .preSignedUrl("https://test-url")
                .build();

        request = UploadingRequestDto.builder().build();
    }

    @Test
    void uploadInit_should_returnUploadingResponseDto_when_successful() {
        Mockito.when(restClient.post()
                .uri(Mockito.anyString())
                .contentType(Mockito.any())
                .accept(Mockito.any())
                .body(request)
                .retrieve()
                .toEntity(UploadingResponseDto.class)
                .getBody()
        ).thenReturn(testResponse);

        UploadingResponseDto actualResponse = client.uploadInit(request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(testResponse.getUploadId(), actualResponse.getUploadId()),
                () -> Assertions.assertEquals(testResponse.getUploadURLs(), actualResponse.getUploadURLs())
        );
    }

    @Test
    void uploadInit_should_throwStorageServiceException_when_responseIsNull() {
        Mockito.when(restClient.post()
                .uri(Mockito.anyString())
                .contentType(Mockito.any())
                .accept(Mockito.any())
                .body(request)
                .retrieve()
                .toEntity(UploadingResponseDto.class)
                .getBody()
        ).thenReturn(null);

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadInit(request));
    }

    @Test
    void uploadInit_should_throwStorageServiceException_when_restClientFails() {
        Mockito.when(restClient.post()
                .uri(Mockito.anyString())
                .contentType(Mockito.any())
                .accept(Mockito.any())
                .body(request)
                .retrieve()
                .toEntity(UploadingResponseDto.class)
                .getBody()
        ).thenThrow(RuntimeException.class);

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadInit(request));
    }

    @Test
    void uploadChunk_should_returnEtag_when_successful() {
        Mockito.when(restClient.put()
                .uri(Mockito.any(URI.class))
                .contentType(Mockito.any())
                .contentLength(Mockito.anyLong())
                .body(Mockito.any(byte[].class))
                .retrieve()
                .toEntity(ResponseEntity.class)
        ).thenReturn(new ResponseEntity<>(headers, HttpStatus.OK));

        String actual = client.uploadChunk(s3Part);

        Assertions.assertEquals(TEST_ETAG_HEADER_VALUE, actual);
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_preSignedUrlIsNull() {
        s3Part.setPreSignedUrl(null);

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_preSignedUrlIsEmpty() {
        s3Part.setPreSignedUrl("");

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_preSignedUrlIsMalformed() {
        s3Part.setPreSignedUrl("htp:/malformed-url");

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_etagIsNull() {
        headers.set(TEST_ETAG_HEADER_KEY, null);

        Mockito.when(restClient.put()
                .uri(Mockito.any(URI.class))
                .contentType(Mockito.any())
                .contentLength(Mockito.anyLong())
                .body(Mockito.any(byte[].class))
                .retrieve()
                .toEntity(ResponseEntity.class)
        ).thenReturn(new ResponseEntity<>(headers, HttpStatus.OK));

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_etagIsEmpty() {
        headers.set(TEST_ETAG_HEADER_KEY, "");

        Mockito.when(restClient.put()
                .uri(Mockito.any(URI.class))
                .contentType(Mockito.any())
                .contentLength(Mockito.anyLong())
                .body(Mockito.any(byte[].class))
                .retrieve()
                .toEntity(ResponseEntity.class)
        ).thenReturn(new ResponseEntity<>(headers, HttpStatus.OK));

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_partDataIsZero() {
        s3Part.setPartData(new byte[0]);

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }

    @Test
    void uploadChunk_should_throwStorageServiceException_when_restClientFails() {
        Mockito.when(restClient.put()
                .uri(Mockito.any(URI.class))
                .contentType(Mockito.any())
                .contentLength(Mockito.anyLong())
                .body(Mockito.any(byte[].class))
                .retrieve()
                .toEntity(ResponseEntity.class)
        ).thenThrow(RuntimeException.class);

        Assertions.assertThrows(StorageServiceException.class, () -> client.uploadChunk(s3Part));
    }
}
