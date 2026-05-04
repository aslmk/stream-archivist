package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import com.aslmk.recordingorchestratorservice.dto.*;
import com.aslmk.recordingorchestratorservice.messaging.kafka.producer.KafkaService;
import com.aslmk.recordingorchestratorservice.messaging.rabbitmq.RabbitMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecordingOrchestrationServiceImpl implements RecordingOrchestrationService {

    private final RabbitMqService rabbitMqService;
    private final KafkaService kafkaService;
    private final RecordedFilePartService recordedFilePartService;
    private final StreamSessionService streamSessionService;

    public RecordingOrchestrationServiceImpl(RabbitMqService rabbitMqService,
                                             KafkaService kafkaService,
                                             RecordedFilePartService recordedFilePartService,
                                             StreamSessionService streamSessionService) {
        this.rabbitMqService = rabbitMqService;
        this.kafkaService = kafkaService;
        this.recordedFilePartService = recordedFilePartService;
        this.streamSessionService = streamSessionService;
    }

    @Override
    public void processStreamEvent(StreamLifecycleEvent event) {
        StreamSessionDto dto = StreamSessionDto.builder()
                .streamerId(event.getStreamerId())
                .status(StreamSessionStatus.RECORDING)
                .build();

        StreamSessionEntity entity = streamSessionService.save(dto);

        RecordStreamJob job = RecordStreamJob.builder()
                .streamId(entity.getStreamId())
                .streamerUsername(event.getStreamerUsername())
                .streamUrl(event.getStreamUrl())
                .build();

        rabbitMqService.sendRecordJob(job);
    }

    @Override
    public void processRecordingEvent(RecordingStatusEvent event) {
        StreamSessionEntity dbSession = streamSessionService
                .getByStreamId(event.getStreamId());

        RecordingStatusUpdatedEvent updatedRecordingEvent = RecordingStatusUpdatedEvent.builder()
                .eventType(event.getEventType())
                .streamerId(dbSession.getStreamerId())
                .build();

        if (event.getEventType().equals(RecordingEventType.RECORDING_STARTED)) {
            kafkaService.publishRecordingUpdatedEvent(updatedRecordingEvent);
            return;
        }

        if (event.getEventType().equals(RecordingEventType.RECORDING_FAILED)) {
            kafkaService.publishRecordingUpdatedEvent(updatedRecordingEvent);
            streamSessionService.updateStatus(event.getStreamId(), StreamSessionStatus.RECORDING_FAILED);
            return;
        }

        kafkaService.publishRecordingUpdatedEvent(updatedRecordingEvent);
        streamSessionService.updateStatus(event.getStreamId(), StreamSessionStatus.UPLOADING);

        UploadStreamRecordJob job = UploadStreamRecordJob.builder()
                .filename(event.getFilename())
                .streamId(event.getStreamId())
                .build();

        rabbitMqService.sendUploadJob(job);
    }

    @Override
    public void processRecordingPartEvent(RecordedPartEvent event) {
        RecordedPartDto dto = RecordedPartDto.builder()
                .filePartName(event.getFilePartName())
                .streamId(event.getStreamId())
                .filePartPath(event.getFilePartPath())
                .partIndex(event.getPartIndex())
                .build();

        if (!recordedFilePartService.saveIfNotExist(dto)) {
            log.debug("Duplicate recording part ignored: streamId='{}', partIndex='{}'",
                    event.getStreamId(), event.getPartIndex());
        }
    }
}
