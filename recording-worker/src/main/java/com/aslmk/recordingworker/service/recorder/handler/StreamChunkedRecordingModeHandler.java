package com.aslmk.recordingworker.service.recorder.handler;

import com.aslmk.recordingworker.dto.RecordedPartEvent;
import com.aslmk.recordingworker.dto.RecordedPartEventType;
import com.aslmk.recordingworker.dto.RecordingEventType;
import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.recorder.RecordingPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class StreamChunkedRecordingModeHandler implements StreamRecordingModeHandler {

    private final ProcessExecutor processExecutor;
    private final KafkaService kafkaService;

    public StreamChunkedRecordingModeHandler(ProcessExecutor processExecutor,
                                             KafkaService kafkaService) {
        this.processExecutor = processExecutor;
        this.kafkaService = kafkaService;
    }

    @Override
    public boolean support(String mode) {
        return "chunked".equalsIgnoreCase(mode);
    }

    @Override
    public void run(RecordingPayload payload) {
        Set<String> processedParts = new HashSet<>();
        List<String> command = buildCommand(payload);

        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                ()-> processExecutor.execute(command));

        Path partsInfoSaveDirectory = payload.saveDirectory().resolve("parts_info.txt");
        long lastFileSize = 0;

        while (!future.isDone()) {
            try {
                if (!Files.exists(partsInfoSaveDirectory)) {
                    Thread.sleep(500);
                    continue;
                }

                long currentFileSize = Files.size(partsInfoSaveDirectory);

                if (currentFileSize > lastFileSize) {

                    try (RandomAccessFile raf = new RandomAccessFile(
                            partsInfoSaveDirectory.toFile(), "r")) {
                        raf.seek(lastFileSize);

                        String line;
                        while ((line = raf.readLine()) != null) {
                            if (processedParts.add(line)) {
                                int partIndex = parsePartIndex(line);
                                publishPartEvent(RecordedPartEventType.PART_RECORDED,
                                        payload, partIndex, line);
                            }
                        }

                        lastFileSize = raf.getFilePointer();
                    }
                }

                Thread.sleep(500);

            } catch (IOException | InterruptedException e) {
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
        String partFileName = payload.streamerUsername() + "_%08d.ts";
        String outputPath = payload.saveDirectory().resolve(partFileName).toString();
        String partsInfoSaveDirectory = payload.saveDirectory()
                .resolve("parts_info.txt").toString();

        String command = "streamlink " + payload.url() + " " + payload.quality() +
                " --stdout | ffmpeg -i pipe:0 -c copy -f segment -segment_time 25 " +
                "-segment_list " + partsInfoSaveDirectory  + " -segment_list_type flat " +
                "-reset_timestamps 1 " + outputPath;

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

    private int parsePartIndex(String line) {
        StringBuilder partIndex = new StringBuilder();

        // File parts saved in the format: 'streamer_username_%08d.ts'
        for (int currChar = line.lastIndexOf("_")+1; currChar < line.length() - 1; currChar++) {
            if (line.charAt(currChar) == '.') break;
            partIndex.append(line.charAt(currChar));
        }

        return Integer.parseInt(partIndex.toString());
    }
}
