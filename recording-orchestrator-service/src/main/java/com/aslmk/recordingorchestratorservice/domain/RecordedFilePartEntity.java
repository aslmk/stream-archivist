package com.aslmk.recordingorchestratorservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recorded_file_parts")
public class RecordedFilePartEntity {
    @EmbeddedId
    private RecordedFilePartId id;

    @Column(name = "file_part_name", nullable = false)
    private String filePartName;

    @Column(name = "file_part_path", nullable = false)
    private String filePartPath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
