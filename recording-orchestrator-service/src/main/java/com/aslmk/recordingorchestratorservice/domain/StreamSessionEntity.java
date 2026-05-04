package com.aslmk.recordingorchestratorservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "stream_sessions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "stream_id", nullable = false, unique = true)
    private UUID streamId;

    @Column(name = "streamer_id", nullable = false)
    private UUID streamerId;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
