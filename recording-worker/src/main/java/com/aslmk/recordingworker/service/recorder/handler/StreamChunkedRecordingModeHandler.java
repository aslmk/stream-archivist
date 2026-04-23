package com.aslmk.recordingworker.service.recorder.handler;

import com.aslmk.recordingworker.dto.RecordedPartEvent;
import com.aslmk.recordingworker.dto.RecordedPartEventType;
import com.aslmk.recordingworker.dto.RecordingEventType;
import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import com.aslmk.recordingworker.service.PartsInfoService;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.recorder.RecordingPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class StreamChunkedRecordingModeHandler implements StreamRecordingModeHandler {

    private final ProcessExecutor processExecutor;
    private final KafkaService kafkaService;
    private final PartsInfoService partsInfoService;

    public StreamChunkedRecordingModeHandler(ProcessExecutor processExecutor,
                                             KafkaService kafkaService,
                                             PartsInfoService partsInfoService) {
        this.processExecutor = processExecutor;
        this.kafkaService = kafkaService;
        this.partsInfoService = partsInfoService;
    }

    @Override
    public boolean support(String mode) {
        return "chunked".equalsIgnoreCase(mode);
    }

    @Override
    public void run(RecordingPayload payload) {
        List<String> command = buildCommand(payload);

        publishRecordingEvent(RecordingEventType.RECORDING_STARTED, payload);

        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                ()-> processExecutor.execute(command))
                .exceptionally(ex -> false);

        Set<String> processedParts = new HashSet<>();
        while (!future.isDone()) {
            try {
                Optional<String> watchForNewRecordedPart = partsInfoService
                        .watchForNewRecordedPart(payload.streamerUsername());

                if (watchForNewRecordedPart.isEmpty()) continue;

                String lastRecordedPartName = watchForNewRecordedPart.get();

                if (processedParts.add(lastRecordedPartName)) {
                    long partIndex = partsInfoService.getLastRecordedPartIndex(payload.streamerUsername());
                    publishPartEvent(RecordedPartEventType.PART_RECORDED,
                            payload, (int) partIndex, lastRecordedPartName);
                }
            } catch (RuntimeException e) {
                log.error("Failed to check recorded parts", e);
                throw new RuntimeException(e);
            }
        }

        future.thenAccept(result -> {
            if (result) {
                publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, payload);
            } else {
                publishRecordingEvent(RecordingEventType.RECORDING_FAILED, payload);
            }
        });
    }

    private List<String> buildCommand(RecordingPayload payload) {
        String filePartPath = partsInfoService.getFilePartPath(payload.streamerUsername());
        Path partsInfoSaveDirectory = partsInfoService.getPartsInfoPath(payload.streamerUsername());

        long lastPartIndex = partsInfoService.getLastRecordedPartIndex(payload.streamerUsername());
        long startPartIndex = lastPartIndex + 1;

        String command = "streamlink " + payload.url() + " " + payload.quality() +
                " --stdout | ffmpeg -i pipe:0 -c copy -f segment -segment_time 25 " +
                "-segment_start_number " + startPartIndex +
                " -segment_list " + partsInfoSaveDirectory  + " -segment_list_type flat " +
                "-reset_timestamps 1 " + filePartPath;

        return List.of("bash", "-c", command);
    }

    private void publishPartEvent(RecordedPartEventType eventType,
                                  RecordingPayload payload, Integer partIndex, String filePartName) {
        log.debug("Publishing '{}' event: partIndex='{}', streamId='{}', filePartName='{}'",
                eventType, partIndex, payload.streamId(), filePartName);

        RecordedPartEvent event = RecordedPartEvent.builder()
                .streamId(payload.streamId())
                .filePartName(filePartName)
                .filePartPath(payload.saveDirectory().toString())
                .eventType(eventType)
                .partIndex(partIndex)
                .build();

        kafkaService.send(event);
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
