package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.AuditLogJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.AuditLogPersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpringDataAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaAuditLogRepositoryAdapterTest {

  @Mock
  private SpringDataAuditLogRepository springDataAuditLogRepository;

  private JpaAuditLogRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new JpaAuditLogRepositoryAdapter(springDataAuditLogRepository, new AuditLogPersistenceMapper());
  }

  @Test
  void savesAuditLogAndReturnsMappedSavedEntity() {
    var timestamp = LocalDateTime.of(2026, 9, 29, 15, 0);
    var auditLog = AuditLog.builder()
      .endpointUrl("/product")
      .httpMethod("GET")
      .statusResponse("FAILED")
      .responseTimeMs(125L)
      .timestamp(timestamp)
      .errorMessage("Catalog unavailable")
      .build();
    when(springDataAuditLogRepository.save(any(AuditLogJpaEntity.class)))
      .thenAnswer(invocation -> {
        AuditLogJpaEntity entity = invocation.getArgument(0);
        entity.setAuditLogId(7L);
        return entity;
      });

    AuditLog savedAuditLog = adapter.save(auditLog);

    assertEquals(7L, savedAuditLog.getAuditLogId());
    assertEquals("/product", savedAuditLog.getEndpointUrl());
    assertEquals("GET", savedAuditLog.getHttpMethod());
    assertEquals("FAILED", savedAuditLog.getStatusResponse());
    assertEquals(125L, savedAuditLog.getResponseTimeMs());
    assertEquals(timestamp, savedAuditLog.getTimestamp());
    assertEquals("Catalog unavailable", savedAuditLog.getErrorMessage());

    var entityCaptor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
    verify(springDataAuditLogRepository).save(entityCaptor.capture());
    var persistedEntity = entityCaptor.getValue();
    assertEquals("/product", persistedEntity.getEndpointUrl());
    assertEquals("GET", persistedEntity.getHttpMethod());
    assertEquals("FAILED", persistedEntity.getStatusResponse());
    assertEquals(125L, persistedEntity.getResponseTimeMs());
    assertEquals(timestamp, persistedEntity.getTimestamp());
    assertEquals("Catalog unavailable", persistedEntity.getErrorMessage());
  }

  @Test
  void propagatesRepositoryErrorsWhenSavingAuditLog() {
    var auditLog = AuditLog.builder()
      .endpointUrl("/product")
      .httpMethod("GET")
      .statusResponse("SUCCESS")
      .responseTimeMs(25L)
      .timestamp(LocalDateTime.of(2026, 9, 29, 15, 0))
      .build();
    var repositoryException = new IllegalStateException("Audit database unavailable");
    when(springDataAuditLogRepository.save(any(AuditLogJpaEntity.class)))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class, () -> adapter.save(auditLog));

    assertSame(repositoryException, exception);
    verify(springDataAuditLogRepository)
      .save(any(AuditLogJpaEntity.class));
  }


}
