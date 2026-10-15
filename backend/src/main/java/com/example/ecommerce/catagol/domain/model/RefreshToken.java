package com.example.ecommerce.catagol.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {

  @EqualsAndHashCode.Exclude
  private UUID id;

  private Long userId;

  private UUID familyId;

  private String tokenHash;

  private Instant createdAt;

  private Instant expiresAt;

  private Instant revokedAt;

  private String replacedBy;

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isExpired(Instant now) {
    return now.isAfter(expiresAt);
  }

  public void revoke(Instant now, String replacedByHash) {
    this.revokedAt = now;
    this.replacedBy = replacedByHash;
  }


}
