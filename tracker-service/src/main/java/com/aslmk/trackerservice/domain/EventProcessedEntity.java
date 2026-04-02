package com.aslmk.trackerservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventProcessedEntity {
    @Id
    @Column(nullable = false)
    private String id;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
