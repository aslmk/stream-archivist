package com.aslmk.recordingworker.service.recorder.handler;

import com.aslmk.recordingworker.dto.RecordedPartEvent;
import com.aslmk.recordingworker.dto.RecordedPartEventType;
import com.aslmk.recordingworker.dto.RecordingEventType;
import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import com.aslmk.recordingworker.service.PartsInfoService;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.StitcherService;
import com.aslmk.recordingworker.service.recorder.RecordingPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class StreamChunkedRecordingModeHandler implements StreamRecordingModeHandler {

    private final ProcessExecutor processExecutor;
    private final KafkaService kafkaService;
    private final PartsInfoService partsInfoService;
    private final StitcherService stitcherService;

    public StreamChunkedRecordingModeHandler(ProcessExecutor processExecutor,
                                             KafkaService kafkaService,
                                             PartsInfoService partsInfoService,
                                             StitcherService stitcherService) {
        this.processExecutor = processExecutor;
        this.kafkaService = kafkaService;
        this.partsInfoService = partsInfoService;
        this.stitcherService = stitcherService;
    }

    @Override
    public boolean support(String mode) {
        return "chunked".equalsIgnoreCase(mode);
    }

    @Override
    public void run(RecordingPayload payload) {
        try {
            log.debug("Starting stream recording",
                    kv("streamId", payload.streamId()),
                    kv("streamerUsername", payload.streamerUsername()),
                    kv("mode", "chunked"));

            Set<String> processedParts = new HashSet<>();
            List<String> command = buildRecordingCommandFromLastPart(payload);

            publishRecordingEvent(RecordingEventType.RECORDING_STARTED, payload);
            stitcherService.init(payload.streamerUsername());

            if (partsInfoService.isPartsInfoExists(payload.streamerUsername())) {
                log.debug("parts-info file is already exists! " +
                        "Fetching recorded parts before starting new recording process",
                        kv("key", payload.streamerUsername()));

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
                    stitcherService.append(payload.streamerUsername(), lastRecordedPartName);
                    publishRecordedPartEvent(payload, lastRecordedPartName);
                }
            }

            Optional<String> remaining;
            while ((remaining = partsInfoService
                    .watchForNewRecordedPart(payload.streamerUsername()))
                    .isPresent()) {
                if (processedParts.add(remaining.get())) {
                    stitcherService.append(payload.streamerUsername(), remaining.get());
                    publishRecordedPartEvent(payload, remaining.get());
                }
            }

            boolean recordingResult = future.join();
            boolean stitchingResult = stitcherService.stitch(payload.streamerUsername(), payload.filename());

            if (recordingResult && stitchingResult) {
                publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, payload);
            } else {
                boolean tryStitchParts = stitcherService
                        .stitch(payload.streamerUsername(), payload.filename());
                if (tryStitchParts) {
                    publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, payload);

                    log.info("Recording and stitching finished",
                            kv("streamId", payload.streamId()),
                            kv("filename", payload.filename()));
                } else {
                    publishRecordingEvent(RecordingEventType.RECORDING_FAILED, payload);
                    log.info("Recording and stitching failed",
                            kv("streamId", payload.streamId()),
                            kv("filename", payload.filename()));
                }
            }
        } catch (RuntimeException e) {
            log.error("Recording failed",
                    kv("streamId", payload.streamId()),
                    kv("streamerUsername", payload.streamerUsername()),
                    e);
            throw e;
        } finally {
            partsInfoService.clearPendingFileParts(payload.streamerUsername());
            stitcherService.clearStitchedParts(payload.streamerUsername());
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
        RecordedPartEvent event = RecordedPartEvent.builder()
                .streamId(payload.streamId())
                .filePartName(filePartName)
                .filename(payload.filename())
                .filePartPath(payload.saveDirectory().toString())
                .eventType(RecordedPartEventType.PART_RECORDED)
                .partIndex(partIndex)
                .build();

        kafkaService.send(event);
        log.debug("Published recorded part event",
                kv("eventType", RecordedPartEventType.PART_RECORDED),
                kv("streamId", payload.streamId()),
                kv("filePartName", filePartName),
                kv("partIndex", partIndex));
    }

    private void publishRecordedPartEvent(RecordingPayload payload, String recordedPart) {
        long partIndex = partsInfoService.getRecordedPartIndex(recordedPart);
        publishPartEvent(payload, (int) partIndex, recordedPart);
    }

    private void publishRecordingEvent(RecordingEventType eventType,
                                       RecordingPayload payload) {
        RecordingStatusEvent event = RecordingStatusEvent.builder()
                .eventType(eventType)
                .filename(payload.filename())
                .streamId(payload.streamId())
                .build();

        kafkaService.send(event);
        log.debug("Published event",
                kv("eventType", eventType),
                kv("streamId", payload.streamId()));
    }
}
