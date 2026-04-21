package com.aslmk.recordingorchestratorservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class RecordedFilePartId implements Serializable {
    @Column(name = "stream_id", nullable = false)
    private UUID streamId;

    @Column(name = "part_index", nullable = false)
    private Integer partIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordedFilePartId that = (RecordedFilePartId) o;
        return Objects.equals(streamId, that.streamId) && Objects.equals(partIndex, that.partIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, partIndex);
    }
}
