package com.aslmk.authservice.domain.auth;

import com.aslmk.authservice.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "providers")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProviderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "provider_name")
    @ColumnTransformer(write = "?::provider")
    private ProviderName providerName;

    @Column(nullable = false, unique = true, name = "provider_user_id")
    private String providerUserId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TokenEntity token;
}
