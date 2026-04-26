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
        try {
            Set<String> processedParts = new HashSet<>();
            List<String> command = buildRecordingCommandFromLastPart(payload);

            publishRecordingEvent(RecordingEventType.RECORDING_STARTED, payload);

            if (partsInfoService.isPartsInfoExists(payload.streamerUsername())) {
                List<String> recordedParts = partsInfoService
                        .getRecordedParts(payload.streamerUsername());

                for (String recordedPart: recordedParts) {
                    if (processedParts.add(recordedPart)) {
                        publishRecordedPartEvent(payload, recordedPart);
                    }
                }
            }

            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                            ()-> processExecutor.execute(command))
                    .exceptionally(ex -> false);

            while (!future.isDone()) {
                Optional<String> watchForNewRecordedPart = partsInfoService
                        .watchForNewRecordedPart(payload.streamerUsername());

                if (watchForNewRecordedPart.isEmpty()) continue;

                String lastRecordedPartName = watchForNewRecordedPart.get();

                if (processedParts.add(lastRecordedPartName)) {
                    publishRecordedPartEvent(payload, lastRecordedPartName);
                }
            }

            Optional<String> remaining;
            while ((remaining = partsInfoService
                    .watchForNewRecordedPart(payload.streamerUsername()))
                    .isPresent()) {
                if (processedParts.add(remaining.get())) {
                    publishRecordedPartEvent(payload, remaining.get());
                }
            }

            boolean result = future.join();

            if (result) {
                publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, payload);
            } else {
                publishRecordingEvent(RecordingEventType.RECORDING_FAILED, payload);
            }

        } catch (RuntimeException e) {
            log.error("Recording failed for streamer '{}'", payload.streamerUsername(), e);
            throw e;
        } finally {
            partsInfoService.clearPendingFileParts(payload.streamerUsername());
        }
    }

    private List<String> buildRecordingCommandFromLastPart(RecordingPayload payload) {
        String filePartPath = partsInfoService.getFilePartPath(payload.streamerUsername());
        Path partsInfoPath = partsInfoService.getPartsInfoPath(payload.streamerUsername());

        long lastPartIndex = partsInfoService
                .getLastRecordedPartName(partsInfoPath)
                .map(partsInfoService::getRecordedPartIndex)
                .orElse(0L);

        long startPartIndex = lastPartIndex + 1;

        String streamlinkPart = String.format("streamlink %s %s --stdout",
                payload.url(), payload.quality());

        String ffmpegPart = String.format("ffmpeg -i pipe:0 -c copy -f segment -segment_time 25" +
                        " -segment_start_number %d" +
                        " -segment_list %s -segment_list_type flat %s",
                startPartIndex, partsInfoPath, filePartPath);

        String command = streamlinkPart + " | " + ffmpegPart;
        return List.of("bash", "-c", command);
    }

    private void publishPartEvent(RecordingPayload payload, Integer partIndex, String filePartName) {
        log.debug("Publishing '{}' event: partIndex='{}', streamId='{}', filePartName='{}'",
                RecordedPartEventType.PART_RECORDED, partIndex, payload.streamId(), filePartName);

        RecordedPartEvent event = RecordedPartEvent.builder()
                .streamId(payload.streamId())
                .filePartName(filePartName)
                .filename(payload.filename())
                .filePartPath(payload.saveDirectory().toString())
                .eventType(RecordedPartEventType.PART_RECORDED)
                .partIndex(partIndex)
                .build();

        kafkaService.send(event);
    }

    private void publishRecordedPartEvent(RecordingPayload payload, String recordedPart) {
        long partIndex = partsInfoService.getRecordedPartIndex(recordedPart);
        log.debug("Publishing recorded part: name='{}', partIndex='{}'",
                recordedPart, partIndex);
        publishPartEvent(payload, (int) partIndex, recordedPart);
    }

    private void publishRecordingEvent(RecordingEventType eventType,
                                       RecordingPayload payload) {
        RecordingStatusEvent event = RecordingStatusEvent.builder()
                .eventType(eventType)
                .filename(payload.filename())
                .streamerUsername(payload.streamerUsername())
                .streamerId(payload.streamerId())
                .streamId(payload.streamId())
                .chunked(true)
                .build();

        kafkaService.send(event);
    }
}
