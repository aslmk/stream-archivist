package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.dto.RecordedPartDto;
import com.aslmk.recordingorchestratorservice.dto.RecordedPartEvent;
import com.aslmk.recordingorchestratorservice.dto.RecordingStatusEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import com.aslmk.recordingorchestratorservice.messaging.rabbitmq.RabbitMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecordingOrchestrationServiceImpl implements RecordingOrchestrationService {

    private final RabbitMqService rabbitMqService;
    private final RecordedFilePartService recordedFilePartService;

    public RecordingOrchestrationServiceImpl(RabbitMqService rabbitMqService,
                                             RecordedFilePartService recordedFilePartService) {
        this.rabbitMqService = rabbitMqService;
        this.recordedFilePartService = recordedFilePartService;
    }

    @Override
    public void processStreamEvent(StreamLifecycleEvent event) {
        rabbitMqService.sendMessage(event);
    }

    @Override
    public void processRecordingEvent(RecordingStatusEvent event) {
        rabbitMqService.sendMessage(event);
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
            log.info("Duplicate recording part ignored: streamId='{}', partIndex='{}'",
                    event.getStreamId(), event.getPartIndex());
        }
    }
}
