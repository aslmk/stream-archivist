package com.aslmk.uploadingworker;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.config.RecordingStorageProperties;
import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.exception.FilePartUploadException;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import com.aslmk.uploadingworker.exception.StreamUploadException;
import com.aslmk.uploadingworker.service.FileSplitterService;
import com.aslmk.uploadingworker.service.S3UploaderService;
import com.aslmk.uploadingworker.service.StreamUploaderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StreamUploaderServiceUnitTests {
    @Mock
    private RecordingStorageProperties properties;
    @Mock
    private FileSplitterService fileSplitterService;
    @Mock
    private StorageServiceClient storageServiceClient;
    @Mock
    private S3UploaderService uploaderService;
    @InjectMocks
    private StreamUploaderServiceImpl streamUploaderService;

    private static final String TEST_FILENAME = "25_09_2025_test0.mp4";
    private UploadStreamRecordJob validJob;
    private Map<Integer, FilePartData> fileParts;
    private InitUploadingResponse initResponse;
    private UploadPartsInfo uploadPartsInfo;

    @BeforeEach
    public void setUp() {
        validJob = buildUploadJob(TEST_FILENAME, UUID.randomUUID());

        Mockito.lenient().when(properties.getPath()).thenReturn("common/recordings");

        fileParts = Map.of(1, new FilePartData(0, 123L));

        initResponse = new InitUploadingResponse("testUploadId");

        uploadPartsInfo = new UploadPartsInfo(
                List.of(new PreSignedUrl(1, "http://s3.test/upload1")),
                null, false);
    }

    @Test
    void processUploadingJob_should_succeed_when_allServicesWorkCorrectly() {
        mockHappyPath();
        Assertions.assertDoesNotThrow(() -> streamUploaderService.processUploadingJob(validJob));
        Mockito.verify(fileSplitterService).getFileParts(Mockito.any());
        Mockito.verify(storageServiceClient).initUpload(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.times(2)).getUploadParts(Mockito.anyString(), Mockito.any());
        Mockito.verify(uploaderService).upload(Mockito.any());
    }

    @Test
    void processUploadingJob_should_loopUntilHasNextIsFalse() {
        UploadPartsInfo firstPartsInfo = new UploadPartsInfo(
                List.of(new PreSignedUrl(1, "http://s3.test/upload1")),
                2, true);
        UploadPartsInfo secondPartsInfo = new UploadPartsInfo(
                List.of(new PreSignedUrl(2, "http://s3.test/upload2")),
                null, false);

        Mockito.when(fileSplitterService.getFileParts(Mockito.any())).thenReturn(fileParts);
        Mockito.when(storageServiceClient.initUpload(Mockito.any())).thenReturn(initResponse);
        Mockito.when(storageServiceClient.getUploadParts(Mockito.anyString(), Mockito.any()))
                .thenReturn(firstPartsInfo)
                .thenReturn(secondPartsInfo);

        Assertions.assertDoesNotThrow(() -> streamUploaderService.processUploadingJob(validJob));

        Mockito.verify(storageServiceClient).initUpload(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.times(3))
                .getUploadParts(Mockito.anyString(), Mockito.any());
        Mockito.verify(uploaderService, Mockito.times(2))
                .upload(Mockito.any());
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_fileSplitterService_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenThrow(FileSplittingException.class);
        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(validJob));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_storageServiceClient_initUpload_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any())).thenReturn(fileParts);
        Mockito.when(storageServiceClient.initUpload(Mockito.any()))
                .thenThrow(StorageServiceException.class);
        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(validJob));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_getUploadParts_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any())).thenReturn(fileParts);
        Mockito.when(storageServiceClient.initUpload(Mockito.any())).thenReturn(initResponse);
        Mockito.when(storageServiceClient.getUploadParts(Mockito.anyString(), Mockito.any()))
                .thenThrow(StorageServiceException.class);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(validJob));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_S3UploaderService_throwsException() {
        mockHappyPath();
        Mockito.doThrow(FilePartUploadException.class).when(uploaderService).upload(Mockito.any());

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(validJob));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_streamIdIsNull() {
        UploadStreamRecordJob job = buildUploadJob(TEST_FILENAME, null);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(job));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_filenameIsNull() {
        UploadStreamRecordJob job = buildUploadJob(null, UUID.randomUUID());

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(job));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_filenameIsEmpty() {
        UploadStreamRecordJob job = buildUploadJob("", UUID.randomUUID());

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(job));
    }

    @Test
    void processUploadingJob_should_throwStreamUploadException_when_filenameIsInvalid() {
        UploadStreamRecordJob job = buildUploadJob("invalid!*#)H\0.mp4", UUID.randomUUID());

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingJob(job));
    }

    @Test
    void processUploadingJob_should_call_getFileParts_with_correct_filename() {
        mockHappyPath();
        streamUploaderService.processUploadingJob(validJob);
        ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);
        Mockito.verify(fileSplitterService).getFileParts(captor.capture());
        Path actualFilePath = captor.getValue();
        String expectedFilePath = properties.getPath() + "/" + validJob.getFilename();
        Assertions.assertTrue(actualFilePath.endsWith(expectedFilePath));
    }

    private UploadStreamRecordJob buildUploadJob(String filename, UUID streamId) {
        return UploadStreamRecordJob.builder()
                .filename(filename)
                .streamId(streamId)
                .build();
    }

    private void mockHappyPath() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenReturn(fileParts);
        Mockito.when(storageServiceClient.initUpload(Mockito.any()))
                .thenReturn(initResponse);
        Mockito.when(storageServiceClient.getUploadParts(Mockito.anyString(), Mockito.any()))
                .thenReturn(uploadPartsInfo);
    }
}