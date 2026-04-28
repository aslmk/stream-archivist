package com.aslmk.uploadingworker;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.dto.FilePartData;
import com.aslmk.uploadingworker.dto.PreSignedUrl;
import com.aslmk.uploadingworker.dto.S3Part;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.exception.FilePartUploadException;
import com.aslmk.uploadingworker.service.S3UploaderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class S3UploaderServiceTests {

    @Mock
    private StorageServiceClient client;

    @InjectMocks
    private S3UploaderServiceImpl service;

    @TempDir
    Path tempDir;

    private static final long TMP_FILE_SIZE = 120L;
    private static final long TMP_CHUNK_SIZE = 50L;
    private static final String UPLOAD_URL = "https://test-upload-url";

    private Path tmpFile;
    private List<PreSignedUrl> uploadUrls;
    private Map<Integer, FilePartData> fileParts;

    @BeforeEach
    void setUp() throws IOException {
        tmpFile = tempDir.resolve("testFile.txt");

        try (RandomAccessFile raf = new RandomAccessFile(tmpFile.toFile(), "rw")) {
            raf.setLength(TMP_FILE_SIZE);
        }

        fileParts = buildExpectedParts();
        uploadUrls = fileParts.keySet().stream()
                .sorted()
                .map(partNum -> new PreSignedUrl(partNum, UPLOAD_URL + "-" + partNum))
                .toList();
    }

    @Test
    void upload_should_uploadAllParts_when_fileIsValid() {
        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(uploadUrls)
                .fileParts(fileParts)
                .filePath(tmpFile.toString())
                .build();

        Assertions.assertDoesNotThrow(() -> service.upload(request));

        Mockito.verify(client, Mockito.times(fileParts.size())).uploadPart(
                Mockito.any(S3Part.class));
    }

    @Test
    void upload_should_throwFilePartUploadException_when_filePathDoesNotExist() {
        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(uploadUrls)
                .fileParts(fileParts)
                .filePath("invalid/file/path")
                .build();

        Assertions.assertThrows(FilePartUploadException.class, () -> service.upload(request));
    }

    @Test
    void upload_should_throwFilePartUploadException_when_clientThrowsException() {
        Mockito.doThrow(RestClientException.class).when(client).uploadPart(Mockito.any(S3Part.class));

        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(uploadUrls)
                .fileParts(fileParts)
                .filePath(tmpFile.toString())
                .build();

        Assertions.assertThrows(FilePartUploadException.class, () -> service.upload(request));
    }

    @Test
    void upload_should_throw_FilePartUploadException_when_readBeyondEOF() {
        Map<Integer, FilePartData> invalidParts = Map.of(
                1, new FilePartData(0, TMP_FILE_SIZE + 100)
        );

        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(List.of(new PreSignedUrl(1, UPLOAD_URL)))
                .fileParts(invalidParts)
                .filePath(tmpFile.toString())
                .build();

        Assertions.assertThrows(FilePartUploadException.class, () -> service.upload(request));
    }

    @Test
    void upload_should_throwFilePartUploadException_when_partNumberMissingInFileParts() {
        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(List.of(new PreSignedUrl(999, UPLOAD_URL)))
                .fileParts(fileParts)
                .filePath(tmpFile.toString())
                .build();

        Assertions.assertThrows(FilePartUploadException.class, () -> service.upload(request));
        Mockito.verify(client, Mockito.never()).uploadPart(Mockito.any());
    }


    private Map<Integer, FilePartData> buildExpectedParts() {
        Map<Integer, FilePartData> parts = new HashMap<>();
        long partsCount = (TMP_FILE_SIZE / TMP_CHUNK_SIZE) + ((TMP_FILE_SIZE % TMP_CHUNK_SIZE) > 0 ? 1 : 0);
        long offset = 0;

        for (int i = 1; i <= partsCount; i++) {
            long remaining = TMP_FILE_SIZE - offset;
            long size = Math.min(TMP_CHUNK_SIZE, remaining);
            parts.put(i, new FilePartData(offset, size));
            offset += TMP_CHUNK_SIZE;
        }
        return parts;
    }
}