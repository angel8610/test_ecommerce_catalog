package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenJpaEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "token_hash", nullable = false, length = 64, unique = true)
  private String tokenHash;

  @Column(name = "family_id", nullable = false)
  private UUID familyId;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  private LocalDateTime revokedAt;

  @Column(name = "replaced_by", length = 64)
  private String replacedBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  private String userAgent;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @PrePersist
  @PreUpdate
  protected void onCreate() {
    this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
  }


}
