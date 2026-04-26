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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stream_sessions")
public class StreamSessionEntity {
    @Id
    @Column(name = "stream_id", unique = true, nullable = false)
    private UUID streamId;

    @Column(name = "upload_id", unique = true, nullable = false)
    private String uploadId;

    @Column(name = "s3_object_key", unique = true, nullable = false)
    private String s3ObjectKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
