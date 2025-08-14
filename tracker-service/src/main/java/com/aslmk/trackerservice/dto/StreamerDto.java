package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamerDto {
    private String streamerUsername;
    private String streamUrl;
    private String streamQuality;

    @Override
    public String toString() {
        return "StreamerDto [" +
                ";\n streamerUsername=" + streamerUsername +
                ";\n streamUrl=" + streamUrl +
                ";\n streamQuality=" + streamQuality + "]";
    }
}
