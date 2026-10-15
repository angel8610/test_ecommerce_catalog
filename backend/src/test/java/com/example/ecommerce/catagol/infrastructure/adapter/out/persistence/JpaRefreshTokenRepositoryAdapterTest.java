package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.domain.model.RefreshToken;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.RefreshTokenJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.RefreshTokenPersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpringRefreshTokenRepository;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.utils.DataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaRefreshTokenRepositoryAdapterTest {

  @Mock
  private SpringRefreshTokenRepository springRefreshTokenRepository;

  private JpaRefreshTokenRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new JpaRefreshTokenRepositoryAdapter(
      springRefreshTokenRepository,
      new RefreshTokenPersistenceMapper()
    );
  }

  @Test
  void returnsMappedRefreshTokenWhenHashExists() {
    var entity = createEntity(
      "existing-hash",
      LocalDateTime.of(2026, 10, 14, 8, 30),
      null
    );
    when(springRefreshTokenRepository.findByTokenHash("existing-hash"))
      .thenReturn(Optional.of(entity));

    Optional<RefreshToken> result = adapter.findByTokenHash("existing-hash");

    assertEquals(Optional.of(createDomainToken(
      entity.getId(),
      entity.getFamilyId(),
      entity.getUserId(),
      "existing-hash",
      entity.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant(),
      null,
      null
    )), result);
    verify(springRefreshTokenRepository).findByTokenHash("existing-hash");
  }

  @Test
  void returnsEmptyWhenHashDoesNotExist() {
    when(springRefreshTokenRepository.findByTokenHash("missing-hash"))
      .thenReturn(Optional.empty());

    Optional<RefreshToken> result = adapter.findByTokenHash("missing-hash");

    assertEquals(Optional.empty(), result);
    verify(springRefreshTokenRepository).findByTokenHash("missing-hash");
  }

  @Test
  void propagatesRepositoryErrorWhenFindingByHash() {
    var repositoryException = new IllegalStateException("Refresh token database unavailable");
    when(springRefreshTokenRepository.findByTokenHash("existing-hash"))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class,
      () -> adapter.findByTokenHash("existing-hash"));

    assertSame(repositoryException, exception);
  }

  @Test
  void returnsMappedRefreshTokenWhenHashExistsForUpdate() {
    var entity = createEntity(
      "locked-hash",
      LocalDateTime.of(2026, 10, 21, 8, 30),
      LocalDateTime.of(2026, 10, 15, 8, 30)
    );
    when(springRefreshTokenRepository.findByTokenHashForUpdate("locked-hash"))
      .thenReturn(Optional.of(entity));

    Optional<RefreshToken> result = adapter.findByTokenHashForUpdate("locked-hash");

    assertEquals(Optional.of(createDomainToken(
      entity.getId(),
      entity.getFamilyId(),
      entity.getUserId(),
      "locked-hash",
      entity.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant(),
      entity.getRevokedAt().atZone(ZoneId.systemDefault()).toInstant(),
      "replacement-hash"
    )), result);
    verify(springRefreshTokenRepository).findByTokenHashForUpdate("locked-hash");
  }

  @Test
  void returnsEmptyWhenHashDoesNotExistForUpdate() {
    when(springRefreshTokenRepository.findByTokenHashForUpdate("missing-hash"))
      .thenReturn(Optional.empty());

    Optional<RefreshToken> result = adapter.findByTokenHashForUpdate("missing-hash");

    assertEquals(Optional.empty(), result);
    verify(springRefreshTokenRepository).findByTokenHashForUpdate("missing-hash");
  }

  @Test
  void propagatesRepositoryErrorWhenFindingByHashForUpdate() {
    var repositoryException = new IllegalStateException("Refresh token lock unavailable");
    when(springRefreshTokenRepository.findByTokenHashForUpdate("existing-hash"))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class,
      () -> adapter.findByTokenHashForUpdate("existing-hash"));

    assertSame(repositoryException, exception);
  }

  @Test
  void savesMappedEntityAndReturnsMappedRefreshToken() {
    var expiresAt = Instant.parse("2026-10-21T14:30:00Z");
    var token = createDomainToken(
      UUID.fromString("a591ae2c-0a58-49d5-babd-1c1a21d18b85"),
      UUID.fromString("69c5aa9d-398e-45ab-942d-9913ef2c1ff6"),
      42L,
      "new-token-hash",
      expiresAt,
      null,
      null
    );
    when(springRefreshTokenRepository.save(any(RefreshTokenJpaEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    RefreshToken result = adapter.save(token);

    assertEquals(token, result);
    var entityCaptor = ArgumentCaptor.forClass(RefreshTokenJpaEntity.class);
    verify(springRefreshTokenRepository).save(entityCaptor.capture());
    var entity = entityCaptor.getValue();
    assertEquals(token.getId(), entity.getId());
    assertEquals(token.getFamilyId(), entity.getFamilyId());
    assertEquals(token.getUserId(), entity.getUserId());
    assertEquals(token.getTokenHash(), entity.getTokenHash());
    assertEquals(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()), entity.getExpiresAt());
    assertNull(entity.getCreatedAt());
    assertNull(entity.getRevokedAt());
  }

  @Test
  void propagatesRepositoryErrorWhenSavingToken() {
    var token = createDomainToken(
      UUID.randomUUID(),
      UUID.randomUUID(),
      42L,
      "new-token-hash",
      Instant.parse("2026-10-21T14:30:00Z"),
      null,
      null
    );
    var repositoryException = new IllegalStateException("Refresh token save failed");
    when(springRefreshTokenRepository.save(any(RefreshTokenJpaEntity.class)))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class, () -> adapter.save(token));

    assertSame(repositoryException, exception);
  }

  @Test
  void delegatesFamilyRevocationWithFamilyIdAndTimestamp() {
    var familyId = UUID.fromString("69c5aa9d-398e-45ab-942d-9913ef2c1ff6");
    var revokedAt = Instant.parse("2026-10-15T14:30:00Z");

    adapter.revokeFamily(familyId, revokedAt);

    verify(springRefreshTokenRepository).revokeFamily(familyId,
      DataUtil.getLocalDateTimeFromInstant(revokedAt));
  }

  @Test
  void propagatesRepositoryErrorWhenRevokingFamily() {
    var familyId = UUID.fromString("69c5aa9d-398e-45ab-942d-9913ef2c1ff6");
    var revokedAt = Instant.parse("2026-10-15T14:30:00Z");
    var repositoryException = new IllegalStateException("Family revocation failed");
    doThrow(repositoryException)
      .when(springRefreshTokenRepository)
      .revokeFamily(familyId, DataUtil.getLocalDateTimeFromInstant(revokedAt));

    var exception = assertThrows(IllegalStateException.class,
      () -> adapter.revokeFamily(familyId, revokedAt));

    assertSame(repositoryException, exception);
  }

  private RefreshTokenJpaEntity createEntity(
    String tokenHash,
    LocalDateTime expiresAt,
    LocalDateTime revokedAt
  ) {
    return RefreshTokenJpaEntity.builder()
      .id(UUID.fromString("a591ae2c-0a58-49d5-babd-1c1a21d18b85"))
      .familyId(UUID.fromString("69c5aa9d-398e-45ab-942d-9913ef2c1ff6"))
      .userId(42L)
      .tokenHash(tokenHash)
      .expiresAt(expiresAt)
      .revokedAt(revokedAt)
      .replacedBy(revokedAt == null ? null : "replacement-hash")
      .createdAt(LocalDateTime.of(2026, 10, 14, 8, 0))
      .build();
  }

  private RefreshToken createDomainToken(
    UUID id,
    UUID familyId,
    Long userId,
    String tokenHash,
    Instant expiresAt,
    Instant revokedAt,
    String replacedBy
  ) {
    return RefreshToken.builder()
      .id(id)
      .familyId(familyId)
      .userId(userId)
      .tokenHash(tokenHash)
      .expiresAt(expiresAt)
      .revokedAt(revokedAt)
      .replacedBy(replacedBy)
      .build();
  }


}
