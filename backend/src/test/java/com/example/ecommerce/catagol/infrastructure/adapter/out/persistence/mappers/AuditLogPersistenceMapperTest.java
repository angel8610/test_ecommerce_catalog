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
      .endpointUrl("/product")
      .httpMethod("GET")
      .statusResponse("FAILED")
      .responseTimeMs(125L)
      .timestamp(timestamp)
      .errorMessage("Catalog unavailable")
      .build();

    AuditLogJpaEntity entity = mapper.mapToEntity(auditLog);

    assertEquals("/product", entity.getEndpointUrl());
    assertEquals("GET", entity.getHttpMethod());
    assertEquals("FAILED", entity.getStatusResponse());
    assertEquals(125L, entity.getResponseTimeMs());
    assertEquals(timestamp, entity.getTimestamp());
    assertEquals("Catalog unavailable", entity.getErrorMessage());
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
      .endpointUrl("/product")
      .httpMethod("GET")
      .statusResponse("FAILED")
      .responseTimeMs(125L)
      .timestamp(timestamp)
      .errorMessage("Catalog unavailable")
      .build();

    AuditLog auditLog = mapper.mapToDomain(entity);

    assertEquals(7L, auditLog.getAuditLogId());
    assertEquals("/product", auditLog.getEndpointUrl());
    assertEquals("GET", auditLog.getHttpMethod());
    assertEquals("FAILED", auditLog.getStatusResponse());
    assertEquals(125L, auditLog.getResponseTimeMs());
    assertEquals(timestamp, auditLog.getTimestamp());
    assertEquals("Catalog unavailable", auditLog.getErrorMessage());
  }

  @Test
  void returnsNullWhenMappingNullEntityToDomain() {
    assertNull(mapper.mapToDomain(null));
  }


}
