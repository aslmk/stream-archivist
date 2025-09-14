package com.aslmk.common.dto;


import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadCompletedEvent {
    private List<PartUploadResultDto> partUploadResults;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(partUploadResults, that.partUploadResults);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(partUploadResults);
    }
}
