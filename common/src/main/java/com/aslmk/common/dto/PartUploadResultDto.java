package com.aslmk.common.dto;


import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartUploadResultDto {
    private int partNumber;
    private String etag;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartUploadResultDto that = (PartUploadResultDto) o;
        return partNumber == that.partNumber && Objects.equals(etag, that.etag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partNumber, etag);
    }

    @Override
    public String toString() {
        return String.format("PartUploadResultDto: { partNumber= '%d', etag= '%s' }", partNumber, etag);
    }
}
