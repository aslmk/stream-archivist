package com.aslmk.common.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordCompletedEvent {
    private String streamerUsername;
    private String fileName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordCompletedEvent that = (RecordCompletedEvent) o;
        return Objects.equals(streamerUsername, that.streamerUsername) && Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamerUsername, fileName);
    }
}
