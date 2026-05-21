package com.aslmk.storageservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "upload_sessions")
public class UploadSessionEntity {
    @Id
    @Column(name = "stream_id", nullable = false, unique = true)
    private UUID streamId;

    @Column(name = "s3_object_key", nullable = false, unique = true)
    private String s3ObjectKey;

    @Column(name = "upload_id", nullable = false, unique = true)
    private String uploadId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "expected_parts")
    private Integer expectedParts;
}
