package com.aslmk.recordingworker.service.recorder.handler;

import com.aslmk.recordingworker.dto.RecordingEventType;
import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.recorder.RecordingPayload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StreamSingleRecordingModeHandler implements StreamRecordingModeHandler {

    private final ProcessExecutor processExecutor;
    private final KafkaService kafkaService;

    public StreamSingleRecordingModeHandler(ProcessExecutor processExecutor,
                                            KafkaService kafkaService) {
        this.processExecutor = processExecutor;
        this.kafkaService = kafkaService;
    }

    @Override
    public boolean support(String mode) {
        return "single".equalsIgnoreCase(mode);
    }

    @Override
    public void run(RecordingPayload payload) {
        publishRecordingEvent(RecordingEventType.RECORDING_STARTED, payload);

        List<String> command = buildCommand(payload);

        boolean result = processExecutor.execute(command);

        if (result) {
            publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, payload);
        } else {
            publishRecordingEvent(RecordingEventType.RECORDING_FAILED, payload);
        }
    }

    private List<String> buildCommand(RecordingPayload payload) {
        String outputPath = payload.saveDirectory().resolve(payload.filename()).toString();
        return List.of("streamlink", "-o",
                outputPath,
                payload.url(),
                payload.quality());
    }

    private void publishRecordingEvent(RecordingEventType eventType,
                                       RecordingPayload payload) {
        RecordingStatusEvent event = RecordingStatusEvent.builder()
                .eventType(eventType)
                .filename(payload.filename())
                .streamerUsername(payload.streamerUsername())
                .streamerId(payload.streamerId())
                .build();

        kafkaService.send(event);
    }
}
