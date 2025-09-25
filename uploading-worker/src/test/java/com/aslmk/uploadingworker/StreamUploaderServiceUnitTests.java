package com.aslmk.uploadingworker;

import com.aslmk.common.dto.PartUploadResultDto;
import com.aslmk.common.dto.RecordCompletedEvent;
import com.aslmk.common.dto.UploadCompletedEvent;
import com.aslmk.common.dto.UploadingResponseDto;
import com.aslmk.uploadingworker.dto.FilePart;
import com.aslmk.uploadingworker.exception.FileChunkUploadException;
import com.aslmk.uploadingworker.exception.FileSplittingException;
import com.aslmk.uploadingworker.exception.StorageServiceException;
import com.aslmk.uploadingworker.exception.StreamUploadException;
import com.aslmk.uploadingworker.kafka.producer.KafkaService;
import com.aslmk.uploadingworker.service.FileSplitterService;
import com.aslmk.uploadingworker.service.S3UploaderService;
import com.aslmk.uploadingworker.service.StorageServiceClient;
import com.aslmk.uploadingworker.service.impl.StreamUploaderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class StreamUploaderServiceUnitTests {
    @Mock
    private FileSplitterService fileSplitterService;
    @Mock
    private StorageServiceClient storageServiceClient;
    @Mock
    private S3UploaderService uploaderService;
    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private StreamUploaderServiceImpl streamUploaderService;


    private static final String SAVE_DIRECTORY = "data";
    private static final String RECORDINGS_DIR = "recordings";
    private static final String TEST_FILENAME = "25_09_2025_test0.mp4";
    private static final String TEST_STREAMER_USERNAME = "test0";

    private RecordCompletedEvent validEvent;
    private UploadingResponseDto response;
    private List<FilePart> fileParts;
    private List<PartUploadResultDto> uploadResults;

    @BeforeEach
    public void setUp() {
        validEvent = buildRecordCompletedEvent(TEST_FILENAME, TEST_STREAMER_USERNAME);
        ReflectionTestUtils.setField(streamUploaderService, "saveDirectory", SAVE_DIRECTORY);

        fileParts = List.of(new FilePart(1, 0, 123L));

        response = UploadingResponseDto.builder()
                .uploadId("testUploadId")
                .uploadURLs(List.of("http://s3.test/upload1"))
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
        Mockito.verify(storageServiceClient).uploadInit(Mockito.any());
        Mockito.verify(uploaderService).upload(Mockito.any());
        Mockito.verify(kafkaService).send(Mockito.any());
    }

    @Test
    void processUploadingRequest_should_sendMessageToKafka() {
        mockHappyPath();

        streamUploaderService.processUploadingRequest(validEvent);

        ArgumentCaptor<UploadCompletedEvent> captor = ArgumentCaptor.forClass(UploadCompletedEvent.class);

        Mockito.verify(kafkaService).send(captor.capture());

        UploadCompletedEvent actualEvent = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(validEvent.getFileName(), actualEvent.getFilename()),
                () -> Assertions.assertEquals(validEvent.getStreamerUsername(), actualEvent.getStreamerUsername())
        );
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

        Mockito.when(storageServiceClient.uploadInit(Mockito.any()))
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
    void processUploadingRequest_should_throwStreamUploadException_when_KafkaService_throwsException() {
        mockHappyPath();

        Mockito.doThrow(new RuntimeException("Kafka send failed"))
                .when(kafkaService).send(Mockito.any());

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(validEvent));
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_streamerUsernameIsNull() {
        RecordCompletedEvent event = buildRecordCompletedEvent(TEST_FILENAME, null);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void processUploadingRequest_should_throwStreamUploadException_when_streamerUsernameIsEmpty() {
        RecordCompletedEvent event = buildRecordCompletedEvent(TEST_FILENAME, "");

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
        Path expectedFilePath = Paths.get("")
                .resolve(SAVE_DIRECTORY)
                .resolve(RECORDINGS_DIR)
                .resolve(validEvent.getFileName());

        Assertions.assertTrue(actualFilePath.endsWith(expectedFilePath));
    }

    @Test
    void getFilePath_should_throwStreamUploadException_when_filenameIsNull() {
        RecordCompletedEvent event = buildRecordCompletedEvent(null, TEST_STREAMER_USERNAME);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void getFilePath_should_throwStreamUploadException_when_filenameIsEmpty() {
        RecordCompletedEvent event = buildRecordCompletedEvent("", TEST_STREAMER_USERNAME);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    @Test
    void getFilePath_should_throwStreamUploadException_when_filenameIsInvalid() {
        RecordCompletedEvent event = buildRecordCompletedEvent("invalid!*#)H\0.mp4", TEST_STREAMER_USERNAME);

        Assertions.assertThrows(StreamUploadException.class,
                () -> streamUploaderService.processUploadingRequest(event));
    }

    private RecordCompletedEvent buildRecordCompletedEvent(String fileName, String streamerUsername) {
        return RecordCompletedEvent.builder()
                .fileName(fileName)
                .streamerUsername(streamerUsername)
                .build();
    }
    private void mockHappyPath() {
        Mockito.when(fileSplitterService.getFileParts(Mockito.any()))
                .thenReturn(fileParts);
        Mockito.when(storageServiceClient.uploadInit(Mockito.any()))
                .thenReturn(response);
        Mockito.when(uploaderService.upload(Mockito.any()))
                .thenReturn(uploadResults);
    }
}
