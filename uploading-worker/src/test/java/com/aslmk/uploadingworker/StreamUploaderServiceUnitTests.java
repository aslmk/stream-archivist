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
    private static final String TEST_STREAMER_USERNAME = "test0";
    private RecordingStatusEvent validEvent;
    private Map<Integer, FilePartData> fileParts;
    private InitUploadingResponse initResponse;
    private UploadPartsInfo uploadPartsInfo;

    @BeforeEach
    public void setUp() {
        validEvent = buildRecordCompletedEvent(TEST_FILENAME, TEST_STREAMER_USERNAME,
                RecordingEventType.RECORDING_FINISHED);

        Mockito.lenient().when(properties.getPath()).thenReturn("common/recordings");

        fileParts = Map.of(1, new FilePartData(0, 123L));

        initResponse = new InitUploadingResponse("testUploadId");

        uploadPartsInfo = new UploadPartsInfo(
                List.of(new PreSignedUrl(1, "http://s3.test/upload1")),
                null, false);
    }
    @Test
    void processUploadingRequest_should_succeed_when_allServicesWorkCorrectly() {
        mockHappyPath();
        Assertions.assertDoesNotThrow(() -> streamUploaderService.processUploadingRequest(validEvent));
        Mockito.verify(fileSplitterService).getFileParts(Mockito.any());
        Mockito.verify(storageServiceClient).initUpload(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.times(2)).getUploadParts(Mockito.anyString(), Mockito.any());
        Mockito.verify(uploaderService).upload(Mockito.any());
    }
    @Test
    void processUploadingRequest_should_loopUntilHasNextIsFalse() {
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

        Assertions.assertDoesNotThrow(() -> streamUploaderService.processUploadingRequest(validEvent));

        Mockito.verify(storageServiceClient).initUpload(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.times(3))
                .getUploadParts(Mockito.anyString(), Mockito.any());
        Mockito.verify(uploaderService, Mockito.times(2))
                .upload(Mockito.any());
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_fileSplitterService_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenThrow(FileSplittingException.class);
        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_storageServiceClient_initUpload_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any())).thenReturn(fileParts);
        Mockito.when(storageServiceClient.initUpload(Mockito.any()))
                .thenThrow(StorageServiceException.class);
        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_getUploadParts_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any())).thenReturn(fileParts);
        Mockito.when(storageServiceClient.initUpload(Mockito.any())).thenReturn(initResponse);
        Mockito.when(storageServiceClient.getUploadParts(Mockito.anyString(), Mockito.any()))
                .thenThrow(StorageServiceException.class);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_S3UploaderService_throwsException() {
        mockHappyPath();
        Mockito.doThrow(FilePartUploadException.class).when(uploaderService).upload(Mockito.any());

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_streamerUsernameIsNull() {
        RecordingStatusEvent event = buildRecordCompletedEvent(TEST_FILENAME,
                null, RecordingEventType.RECORDING_FINISHED);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_streamerUsernameIsEmpty() {
        RecordingStatusEvent event = buildRecordCompletedEvent(TEST_FILENAME,
                "", RecordingEventType.RECORDING_FINISHED);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_filenameIsNull() {
        RecordingStatusEvent event = buildRecordCompletedEvent(null,
                TEST_STREAMER_USERNAME, RecordingEventType.RECORDING_FINISHED);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_filenameIsEmpty() {
        RecordingStatusEvent event = buildRecordCompletedEvent("",
                TEST_STREAMER_USERNAME, RecordingEventType.RECORDING_FINISHED);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }
    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_filenameIsInvalid() {
        RecordingStatusEvent event = buildRecordCompletedEvent("invalid!*#)H\0.mp4",
                TEST_STREAMER_USERNAME, RecordingEventType.RECORDING_FINISHED);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }
    @Test
    void processUploadingRequest_should_call_getFilePath_with_correct_filename() {
        mockHappyPath();
        streamUploaderService.processUploadingRequest(validEvent);
        ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);
        Mockito.verify(fileSplitterService).getFileParts(captor.capture());
        Path actualFilePath = captor.getValue();
        String expectedFilePath = properties.getPath() + "/" + validEvent.getFilename();
        Assertions.assertTrue(actualFilePath.endsWith(expectedFilePath));
    }
    @Test
    void processUploadingRequest_should_initChunkedUploadAndReturn_when_eventIsChunkedModeAndEventIsRecordingStarted() {
        validEvent.setChunked(true);
        validEvent.setEventType(RecordingEventType.RECORDING_STARTED);

        streamUploaderService.processUploadingRequest(validEvent);

        Mockito.verify(storageServiceClient).initChunkedUpload(Mockito.any(), Mockito.anyString());
        Mockito.verify(fileSplitterService, Mockito.never()).getFileParts(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.never()).initUpload(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.never())
                .getUploadParts(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(uploaderService, Mockito.never()).upload(Mockito.any());
    }
    @Test
    void processUploadingRequest_should_completeChunkedUploadAndReturn_when_eventIsChunkedModeAndEventIsRecordingFinished() {
        validEvent.setChunked(true);
        validEvent.setEventType(RecordingEventType.RECORDING_FINISHED);

        streamUploaderService.processUploadingRequest(validEvent);

        Mockito.verify(storageServiceClient).completeChunkedUpload(Mockito.any(), Mockito.anyString());
        Mockito.verify(fileSplitterService, Mockito.never()).getFileParts(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.never()).initUpload(Mockito.any());
        Mockito.verify(storageServiceClient, Mockito.never())
                .getUploadParts(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(uploaderService, Mockito.never()).upload(Mockito.any());
    }
    @Test
    void processChunkedUploadingRequest_should_getSignedUrlAndUploadRecordedPart() {
        UUID streamId = UUID.randomUUID();
        String filename = "26_04_2026_test_streamer.ts";
        String filePartName = "test_streamer_0001.ts";
        String filePartPath = "/app/recordings";
        Path expectedFilePath = Path.of(filePartPath, filePartName);

        RecordedPartEvent event = RecordedPartEvent.builder()
                .filePartPath(filePartPath)
                .filePartName(filePartName)
                .filename(filename)
                .partIndex(1)
                .streamId(streamId)
                .build();

        PreSignedUrl expectedPreSignedUrl = new PreSignedUrl(1, "http://s3.test/upload1");
        Mockito.when(storageServiceClient.getPreSignedUrl(streamId, filename, 1L))
                .thenReturn(expectedPreSignedUrl);

        streamUploaderService.processChunkedUploadingRequest(event);

        ArgumentCaptor<UploadRecordedPart> captor = ArgumentCaptor.forClass(UploadRecordedPart.class);

        Mockito.verify(storageServiceClient).getPreSignedUrl(streamId, filename, 1L);
        Mockito.verify(uploaderService).uploadPart(captor.capture());

        UploadRecordedPart actual = captor.getValue();
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedFilePath, actual.filePath()),
                () -> Assertions.assertEquals(expectedPreSignedUrl, actual.preSignedUrl()));
    }

    private RecordingStatusEvent buildRecordCompletedEvent(String fileName,
                                                           String streamerUsername,
                                                           RecordingEventType eventType) {
        return RecordingStatusEvent.builder()
                .eventType(eventType)
                .filename(fileName)
                .streamerUsername(streamerUsername)
                .streamId(UUID.randomUUID())
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