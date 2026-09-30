package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.AuditLogJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuditLogPersistenceMapperTest {

  private final AuditLogPersistenceMapper mapper = new AuditLogPersistenceMapper();

  @Test
  void mapsAuditLogToJpaEntity() {
    var timestamp = LocalDateTime.of(2026, 9, 29, 15, 0);
    var auditLog = AuditLog.builder()
      .auditLogId(7L)
      .operation("GET /product")
      .status("FAILED")
      .durationMs(125L)
      .registerDate(timestamp)
      .createdBy("catalog-user")
      .error("Catalog unavailable")
      .build();

    AuditLogJpaEntity entity = mapper.mapToEntity(auditLog);

    assertEquals("GET /product", entity.getOperation());
    assertEquals("FAILED", entity.getStatus());
    assertEquals(125L, entity.getDurationMs());
    assertEquals(timestamp, entity.getRegisterDate());
    assertEquals("catalog-user", entity.getCreatedBy());
    assertEquals("Catalog unavailable", entity.getError());
  }

  @Test
  void returnsNullWhenMappingNullAuditLogToEntity() {
    assertNull(mapper.mapToEntity(null));
  }

  @Test
  void mapsJpaEntityToAuditLog() {
    var timestamp = LocalDateTime.of(2026, 9, 29, 15, 0);
    var entity = AuditLogJpaEntity.builder()
      .auditLogId(7L)
      .operation("GET /product")
      .status("FAILED")
      .durationMs(125L)
      .registerDate(timestamp)
      .createdBy("catalog-user")
      .error("Catalog unavailable")
      .build();

    AuditLog auditLog = mapper.mapToDomain(entity);

    assertEquals(7L, auditLog.getAuditLogId());
    assertEquals("GET /product", auditLog.getOperation());
    assertEquals("FAILED", auditLog.getStatus());
    assertEquals(125L, auditLog.getDurationMs());
    assertEquals(timestamp, auditLog.getRegisterDate());
    assertEquals("catalog-user", auditLog.getCreatedBy());
    assertEquals("Catalog unavailable", auditLog.getError());
  }

  @Test
  void returnsNullWhenMappingNullEntityToDomain() {
    assertNull(mapper.mapToDomain(null));
  }


}
