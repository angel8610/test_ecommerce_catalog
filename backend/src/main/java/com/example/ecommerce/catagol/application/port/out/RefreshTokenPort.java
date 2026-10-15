package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.domain.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenPort {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

  RefreshToken save(RefreshToken token);

  void revokeFamily(UUID familyId, Instant now);


}
