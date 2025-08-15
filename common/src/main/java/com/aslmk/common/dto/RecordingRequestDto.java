package com.aslmk.common.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordingRequestDto {
    private String streamerUsername;
    private String streamUrl;
    private String streamQuality;

    @Override
    public String toString() {
        return "RecordingRequestDto [" +
                "streamerUsername=" + streamerUsername +
                ";\n streamUrl=" + streamUrl +
                ";\n streamQuality=" + streamQuality + "]";
    }
}
