package com.aslmk.uploadingworker;

import com.aslmk.common.dto.PartUploadResultDto;
import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.dto.S3UploadRequestDto;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import com.aslmk.uploadingworker.service.impl.S3UploaderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class S3UploaderServiceUnitTests {
    @Mock
    private StorageServiceClient client;

    @InjectMocks
    private S3UploaderServiceImpl service;

    private static final long TMP_FILE_SIZE = 120 * 1024 * 1024; // 120 MB
    private static final long TMP_CHUNK_SIZE = 50 * 1024 * 1024; // 50 MB
    private static final String UPLOAD_URL = "https://test-upload-url";

    private File tmpFile;
    private List<String> uploadUrls;
    private List<FilePart> fileParts;

    @BeforeEach
    void setUp() throws IOException {
        Path tmpFilePath = Files.createTempFile("testFile", ".txt");
        tmpFile = tmpFilePath.toFile();
        fileParts = getFileParts(tmpFile);

        Assertions.assertTrue(Files.exists(tmpFilePath));
        Assertions.assertEquals(TMP_FILE_SIZE, tmpFile.length());

        uploadUrls = new ArrayList<>();
        for (int i = 0; i < fileParts.size(); i++) {
            uploadUrls.add(UPLOAD_URL + "-" + i);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tmpFile.toPath());
    }

    @Test
    void should_uploadFileToS3_when_fileIsValid() {
        String etag = "etag-string";
        Mockito.when(client.uploadChunk(Mockito.any())).thenReturn(etag);

        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(uploadUrls)
                .fileParts(fileParts)
                .filePath(tmpFile.getPath())
                .build();

        List<PartUploadResultDto> result = service.upload(request);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(fileParts.size(), result.size());

        for (int i = 0; i < fileParts.size(); i++) {
            Assertions.assertEquals(fileParts.get(i).partNumber(), result.get(i).getPartNumber());
            Assertions.assertEquals(etag, result.get(i).getEtag());

        }

        Mockito.verify(client, Mockito.times(fileParts.size())).uploadChunk(Mockito.any());
    }

    @Test
    void should_throwFileChunkUploadException_when_filePathIsInvalid() {
        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(uploadUrls)
                .fileParts(fileParts)
                .filePath("invalid/file/path")
                .build();

        Assertions.assertThrows(FileChunkUploadException.class, () -> service.upload(request));
    }

    @Test
    void should_throwFileChunkUploadException_when_storageServiceClientThrowsAnException() {
        Mockito.when(client.uploadChunk(Mockito.any())).thenThrow(RuntimeException.class);

        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(uploadUrls)
                .fileParts(fileParts)
                .filePath(tmpFile.getPath())
                .build();

        Assertions.assertThrows(FileChunkUploadException.class, () -> service.upload(request));
    }

    @Test
    void should_throwFileChunkUploadException_when_readBeyondEOF() {
        FilePart invalidPart = new FilePart(1, 0, TMP_FILE_SIZE+100);

        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(List.of(UPLOAD_URL))
                .fileParts(List.of(invalidPart))
                .filePath(tmpFile.getPath())
                .build();

        Assertions.assertThrows(FileChunkUploadException.class, () -> service.upload(request));
    }

    @Test
    void should_throwFileChunkUploadException_when_uploadUrlsNoEqualToPartNumbers() {
        S3UploadRequestDto request = S3UploadRequestDto.builder()
                .uploadUrls(List.of(UPLOAD_URL))
                .fileParts(fileParts)
                .filePath(tmpFile.getPath())
                .build();

        Assertions.assertThrows(FileChunkUploadException.class, () -> service.upload(request));

        Mockito.verify(client, Mockito.never()).uploadChunk(Mockito.any());
    }

    private List<FilePart> getFileParts(File tmpFile) throws IOException {
        List<FilePart> fileParts = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw")) {
            raf.setLength(TMP_FILE_SIZE);
        }

        long partsCount = (TMP_FILE_SIZE / TMP_CHUNK_SIZE) + ((TMP_FILE_SIZE % TMP_CHUNK_SIZE) > 0 ? 1 : 0);
        long offset = 0;

        for (int i = 1; i <= partsCount; i++) {
            FilePart part = new FilePart(i, offset, Math.min(TMP_CHUNK_SIZE, TMP_FILE_SIZE -offset));
            offset += TMP_CHUNK_SIZE;
            fileParts.add(part);
        }

        return fileParts;
    }
}
