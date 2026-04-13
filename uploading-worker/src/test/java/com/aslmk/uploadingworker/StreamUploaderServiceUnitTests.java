package com.aslmk.uploadingworker;

import com.aslmk.uploadingworker.client.StorageServiceClient;
import com.aslmk.uploadingworker.config.RecordingStorageProperties;
import com.aslmk.uploadingworker.dto.*;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
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
    private UploadingResponseDto response;
    private Map<Integer, FilePartData> fileParts;
    private List<PartUploadResultDto> uploadResults;

    @BeforeEach
    public void setUp() {
        validEvent = buildRecordCompletedEvent(TEST_FILENAME, TEST_STREAMER_USERNAME);
        Mockito.lenient().when(properties.getPath()).thenReturn("common/recordings");

        fileParts = Map.of(1, new FilePartData(0, 123L));

        response = UploadingResponseDto.builder()
                .uploadId("testUploadId")
                .uploadUrls(List.of(new PreSignedUrl(1, "http://s3.test/upload1")))
                .hasNext(false)
                .nextPartNumberMarker(null)
                .build();

        PartUploadResultDto partUploadResultDto = PartUploadResultDto.builder()
                .partNumber(1)
                .etag("etag1")
                .build();

        uploadResults = List.of(partUploadResultDto);
    }

    @Test
    void processUploadingRequest_should_succeed_when_allServicesWorkCorrectly() {
        mockHappyPath();

        Assertions.assertDoesNotThrow(() -> streamUploaderService.processUploadingRequest(validEvent));

        Mockito.verify(fileSplitterService).getFileParts(Mockito.any());
        Mockito.verify(storageServiceClient).processUpload(Mockito.any());
        Mockito.verify(uploaderService).upload(Mockito.any());
    }

    @Test
    void processUploadingRequest_should_loopUntilHasNextIsFalse() {
        UploadingResponseDto firstResponse = UploadingResponseDto.builder()
                .uploadId("testUploadId")
                .uploadUrls(List.of(new PreSignedUrl(1, "http://s3.test/upload1")))
                .hasNext(true)
                .nextPartNumberMarker(1)
                .build();

        UploadingResponseDto secondResponse = UploadingResponseDto.builder()
                .uploadId("testUploadId")
                .uploadUrls(List.of(new PreSignedUrl(2, "http://s3.test/upload2")))
                .hasNext(false)
                .nextPartNumberMarker(null)
                .build();

        Mockito.when(fileSplitterService.getFileParts(Mockito.any())).thenReturn(fileParts);
        Mockito.when(storageServiceClient.processUpload(Mockito.any()))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);
        Mockito.when(uploaderService.upload(Mockito.any())).thenReturn(uploadResults);

        Assertions.assertDoesNotThrow(() -> streamUploaderService.processUploadingRequest(validEvent));

        Mockito.verify(storageServiceClient, Mockito.times(2)).processUpload(Mockito.any());
        Mockito.verify(uploaderService, Mockito.times(2)).upload(Mockito.any());
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_fileSplitterService_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenThrow(FileSplittingException.class);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_storageServiceClient_throwsException() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenReturn(fileParts);
        Mockito.when(storageServiceClient.processUpload(Mockito.any()))
                .thenThrow(StorageServiceException.class);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_S3UploaderService_throwsException() {
        mockHappyPath();

        Mockito.when(uploaderService.upload(Mockito.any()))
                .thenThrow(FileChunkUploadException.class);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_streamerUsernameIsNull() {
        RecordingStatusEvent event = buildRecordCompletedEvent(TEST_FILENAME, null);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_streamerUsernameIsEmpty() {
        RecordingStatusEvent event = buildRecordCompletedEvent(TEST_FILENAME, "");

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void getFilePath_should_returnValidFilePath_when_filenameIsValid() {
        mockHappyPath();

        streamUploaderService.processUploadingRequest(validEvent);

        ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);

        Mockito.verify(fileSplitterService).getFileParts(captor.capture());

        Path actualFilePath = captor.getValue();
        String expectedFilePath = properties.getPath() + "/" + validEvent.getFilename();

        Assertions.assertTrue(actualFilePath.endsWith(expectedFilePath));
    }

    @Test
    void getFilePath_should_throwStreamUploadException_when_filenameIsNull() {
        RecordingStatusEvent event = buildRecordCompletedEvent(null, TEST_STREAMER_USERNAME);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void getFilePath_should_throwStreamUploadException_when_filenameIsEmpty() {
        RecordingStatusEvent event = buildRecordCompletedEvent("", TEST_STREAMER_USERNAME);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void getFilePath_should_throwStreamUploadException_when_filenameIsInvalid() {
        RecordingStatusEvent event = buildRecordCompletedEvent("invalid!*#)H\0.mp4", TEST_STREAMER_USERNAME);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    private RecordingStatusEvent buildRecordCompletedEvent(String fileName, String streamerUsername) {
        return RecordingStatusEvent.builder()
                .eventType(RecordingEventType.RECORDING_FINISHED)
                .filename(fileName)
                .streamerUsername(streamerUsername)
                .build();
    }

    private void mockHappyPath() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenReturn(fileParts);
        Mockito.when(storageServiceClient.processUpload(Mockito.any()))
                .thenReturn(response);
        Mockito.when(uploaderService.upload(Mockito.any()))
                .thenReturn(uploadResults);
    }
}