package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.RefreshToken;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.RefreshTokenJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RefreshTokenPersistenceMapperTest {

  private final RefreshTokenPersistenceMapper mapper = new RefreshTokenPersistenceMapper();

  @Test
  void mapsJpaEntityToDomainAndConvertsAllTimestampFields() {
    var id = UUID.fromString("77ddaf63-09c8-49a6-94ae-4ce5f53d63d2");
    var familyId = UUID.fromString("dc6304db-76b4-46e4-bd5a-39fa11f1c638");
    var createdAt = LocalDateTime.of(2026, 10, 14, 8, 30, 15, 123_456_000);
    var expiresAt = LocalDateTime.of(2026, 10, 21, 8, 30, 15, 123_456_000);
    var revokedAt = LocalDateTime.of(2026, 10, 15, 9, 45, 30, 654_321_000);
    var entity = createEntity(id, familyId, "token-hash", expiresAt,
      revokedAt, "replacement-hash", createdAt);

    RefreshToken token = mapper.mapToDomain(entity);

    assertEquals(id, token.getId());
    assertEquals(familyId, token.getFamilyId());
    assertEquals(42L, token.getUserId());
    assertEquals("token-hash", token.getTokenHash());
    assertEquals(createdAt.atZone(ZoneId.systemDefault()).toInstant(), token.getCreatedAt());
    assertEquals(expiresAt.atZone(ZoneId.systemDefault()).toInstant(), token.getExpiresAt());
    assertEquals(revokedAt.atZone(ZoneId.systemDefault()).toInstant(), token.getRevokedAt());
    assertEquals("replacement-hash", token.getReplacedBy());
  }

  @Test
  void convertsBlankTokenHashToNullWhenMappingJpaEntityToDomain() {
    var entity = createEntity(
      UUID.randomUUID(),
      UUID.randomUUID(),
      "  \t ",
      LocalDateTime.of(2026, 10, 21, 8, 30),
      null,
      null,
      null
    );

    RefreshToken token = mapper.mapToDomain(entity);

    assertNull(token.getTokenHash());
    assertNull(token.getCreatedAt());
    assertNull(token.getRevokedAt());
    assertEquals(entity.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant(), token.getExpiresAt());
  }

  @Test
  void returnsNullWhenMappingNullJpaEntityToDomain() {
    assertNull(mapper.mapToDomain(null));
  }

  @Test
  void mapsDomainToJpaEntityAndConvertsExpirationTimestamp() {
    var id = UUID.fromString("77ddaf63-09c8-49a6-94ae-4ce5f53d63d2");
    var familyId = UUID.fromString("dc6304db-76b4-46e4-bd5a-39fa11f1c638");
    var expiresAt = Instant.parse("2026-10-21T14:30:15.123456Z");
    var token = RefreshToken.builder()
      .id(id)
      .familyId(familyId)
      .userId(42L)
      .tokenHash("token-hash")
      .createdAt(Instant.parse("2026-10-14T14:30:15Z"))
      .expiresAt(expiresAt)
      .revokedAt(Instant.parse("2026-10-15T15:45:30Z"))
      .replacedBy(" replacement-hash ")
      .build();

    RefreshTokenJpaEntity entity = mapper.mapToEntity(token);

    assertEquals(id, entity.getId());
    assertEquals(familyId, entity.getFamilyId());
    assertEquals(42L, entity.getUserId());
    assertEquals("token-hash", entity.getTokenHash());
    assertEquals(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()), entity.getExpiresAt());
    assertEquals("replacement-hash", entity.getReplacedBy());
    assertNull(entity.getCreatedAt());
    assertNull(entity.getRevokedAt());
  }

  @Test
  void convertsBlankReplacementHashToNullAndPreservesNullExpiration() {
    var token = RefreshToken.builder()
      .userId(42L)
      .tokenHash("token-hash")
      .replacedBy("  ")
      .build();

    RefreshTokenJpaEntity entity = mapper.mapToEntity(token);

    assertNull(entity.getExpiresAt());
    assertNull(entity.getReplacedBy());
  }

  @Test
  void returnsNullWhenMappingNullDomainTokenToEntity() {
    assertNull(mapper.mapToEntity(null));
  }

  private RefreshTokenJpaEntity createEntity(
    UUID id,
    UUID familyId,
    String tokenHash,
    LocalDateTime expiresAt,
    LocalDateTime revokedAt,
    String replacedBy,
    LocalDateTime createdAt
  ) {
    return RefreshTokenJpaEntity.builder()
      .id(id)
      .familyId(familyId)
      .userId(42L)
      .tokenHash(tokenHash)
      .expiresAt(expiresAt)
      .revokedAt(revokedAt)
      .replacedBy(replacedBy)
      .createdAt(createdAt)
      .build();
  }


}
