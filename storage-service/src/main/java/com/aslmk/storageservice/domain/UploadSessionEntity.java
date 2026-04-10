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
    private String s3ObjectPath;

    @Column(nullable = false)
    private String uploadId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
