package com.aslmk.uploadingworker.dto;

import java.util.List;

public record UploadPartsInfo(List<PreSignedUrl> uploadUrls,
                              Integer nextPartNumberMarker,
                              boolean hasNext) {
}
