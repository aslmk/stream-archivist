package com.aslmk.authservice.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private UUID userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
