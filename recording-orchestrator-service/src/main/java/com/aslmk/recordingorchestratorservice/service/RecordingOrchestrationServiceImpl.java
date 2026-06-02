package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.domain.JobType;
import com.aslmk.recordingorchestratorservice.domain.StreamSessionEntity;
import com.aslmk.recordingorchestratorservice.dto.*;
import com.aslmk.recordingorchestratorservice.messaging.kafka.producer.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class RecordingOrchestrationServiceImpl implements RecordingOrchestrationService {

    private final KafkaService kafkaService;
    private final RecordedFilePartService recordedFilePartService;
    private final StreamSessionService streamSessionService;
    private final JobLogService jobLogService;
    private final ProcessedEventService processedEventService;

    public RecordingOrchestrationServiceImpl(KafkaService kafkaService,
                                             RecordedFilePartService recordedFilePartService,
                                             StreamSessionService streamSessionService,
                                             JobLogService jobLogService,
                                             ProcessedEventService processedEventService) {
        this.kafkaService = kafkaService;
        this.recordedFilePartService = recordedFilePartService;
        this.streamSessionService = streamSessionService;
        this.jobLogService = jobLogService;
        this.processedEventService = processedEventService;
    }

    @Override
    public void processStreamEvent(StreamLifecycleEvent event) {
        if (!processedEventService.tryMarkAsProcessed(event.getEventId())) {
            log.debug("Duplicate event ignored",
                    kv("eventId", event.getEventId()),
                    kv("streamerId", event.getStreamerId()));
            return;
        }

        log.debug("Processing event",
                kv("eventType", event.getEventType()),
                kv("streamerId", event.getStreamerId()),
                kv("streamerUsername", event.getStreamerUsername()));

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

        jobLogService.save(job,JobType.RECORD);

        log.info("Processed event",
                kv("eventType", event.getEventType()),
                kv("streamerId", event.getStreamerId()),
                kv("streamerUsername", event.getStreamerUsername()));
    }

    @Override
    public void processRecordingEvent(RecordingStatusEvent event) {
        log.debug("Processing event",
                kv("eventType", event.getEventType()),
                kv("streamId", event.getStreamId()),
                kv("filename", event.getFilename()));

        StreamSessionEntity dbSession = streamSessionService
                .getByStreamId(event.getStreamId());

        RecordingStatusUpdatedEvent updatedRecordingEvent = RecordingStatusUpdatedEvent.builder()
                .eventType(event.getEventType())
                .streamerId(dbSession.getStreamerId())
                .build();

        if (event.getEventType().equals(RecordingEventType.RECORDING_STARTED)) {
            kafkaService.publishRecordingUpdatedEvent(updatedRecordingEvent);
            log.debug("Published recording update event",
                    kv("eventType", updatedRecordingEvent.getEventType()),
                    kv("streamerId", updatedRecordingEvent.getStreamerId()));
            return;
        }

        if (event.getEventType().equals(RecordingEventType.RECORDING_FAILED)) {
            kafkaService.publishRecordingUpdatedEvent(updatedRecordingEvent);
            streamSessionService.updateStatus(event.getStreamId(), StreamSessionStatus.RECORDING_FAILED);
            log.debug("Published recording update event",
                    kv("eventType", updatedRecordingEvent.getEventType()),
                    kv("streamerId", updatedRecordingEvent.getStreamerId()));
            return;
        }

        kafkaService.publishRecordingUpdatedEvent(updatedRecordingEvent);
        streamSessionService.updateStatus(event.getStreamId(), StreamSessionStatus.UPLOADING);

        UploadStreamRecordJob job = UploadStreamRecordJob.builder()
                .filename(event.getFilename())
                .streamId(event.getStreamId())
                .build();

        jobLogService.save(job, JobType.UPLOAD);

        log.debug("Processed event",
                kv("eventType", event.getEventType()),
                kv("streamId", event.getStreamId()),
                kv("filename", event.getFilename()));
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
            log.debug("Duplicate recording part ignored",
                    kv("eventType", event.getEventType()),
                    kv("streamId", event.getStreamId()),
                    kv("partIndex", event.getPartIndex()));
        }
    }
}
