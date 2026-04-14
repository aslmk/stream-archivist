package com.aslmk.storageservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "upload_sessions")
public class UploadSessionEntity {
    @Id
    @Column(name = "s3_object_path")
    private String s3ObjectPath;

    @Column(name = "upload_id", nullable = false)
    private String uploadId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "expected_parts")
    private Integer expectedParts;
}
