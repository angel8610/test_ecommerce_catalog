package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories;

import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.RefreshTokenJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SpringRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

  Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT t FROM RefreshTokenJpaEntity t WHERE t.tokenHash = :hash")
  Optional<RefreshTokenJpaEntity> findByTokenHashForUpdate(@Param("hash") String hash);

  @Modifying
  @Query("UPDATE RefreshTokenJpaEntity t SET t.revokedAt = :now " +
    "WHERE t.familyId = :familyId AND t.revokedAt IS NULL")
  void revokeFamily(@Param("familyId") UUID familyId, @Param("now") LocalDateTime now);


}
