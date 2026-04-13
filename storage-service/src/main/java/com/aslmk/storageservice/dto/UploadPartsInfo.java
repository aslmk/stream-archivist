package com.aslmk.storageservice.dto;

import java.util.List;

public record UploadPartsInfo(List<PreSignedUrl> uploadUrls,
                              Integer nextPartNumberMarker,
                              boolean hasNext) {
}
