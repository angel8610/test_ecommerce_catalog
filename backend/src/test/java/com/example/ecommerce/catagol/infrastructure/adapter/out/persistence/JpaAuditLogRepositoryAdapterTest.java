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
import java.util.List;

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
      .operation("GET /product")
      .status("FAILED")
      .durationMs(125L)
      .time(timestamp)
      .user("catalog-user")
      .error("Catalog unavailable")
      .build();
    when(springDataAuditLogRepository.save(any(AuditLogJpaEntity.class)))
      .thenAnswer(invocation -> {
        AuditLogJpaEntity entity = invocation.getArgument(0);
        entity.setAuditLogId(7L);
        return entity;
      });

    AuditLog savedAuditLog = adapter.save(auditLog);

    assertEquals(7L, savedAuditLog.getAuditLogId());
    assertEquals("GET /product", savedAuditLog.getOperation());
    assertEquals("FAILED", savedAuditLog.getStatus());
    assertEquals(125L, savedAuditLog.getDurationMs());
    assertEquals(timestamp, savedAuditLog.getTime());
    assertEquals("catalog-user", savedAuditLog.getUser());
    assertEquals("Catalog unavailable", savedAuditLog.getError());

    var entityCaptor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
    verify(springDataAuditLogRepository).save(entityCaptor.capture());
    var persistedEntity = entityCaptor.getValue();
    assertEquals("GET /product", persistedEntity.getOperation());
    assertEquals("FAILED", persistedEntity.getStatus());
    assertEquals(125L, persistedEntity.getDurationMs());
    assertEquals(timestamp, persistedEntity.getTime());
    assertEquals("catalog-user", persistedEntity.getUser());
    assertEquals("Catalog unavailable", persistedEntity.getError());
  }

  @Test
  void propagatesRepositoryErrorsWhenSavingAuditLog() {
    var auditLog = AuditLog.builder()
      .operation("GET /product")
      .status("SUCCESS")
      .durationMs(25L)
      .time(LocalDateTime.of(2026, 9, 29, 15, 0))
      .build();
    var repositoryException = new IllegalStateException("Audit database unavailable");
    when(springDataAuditLogRepository.save(any(AuditLogJpaEntity.class)))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class, () -> adapter.save(auditLog));

    assertSame(repositoryException, exception);
    verify(springDataAuditLogRepository)
      .save(any(AuditLogJpaEntity.class));
  }

  @Test
  void findAllAuditLogsAndMapsEntitiesToDomain() {
    var firstTimestamp = LocalDateTime.of(2026, 9, 29, 15, 0);
    var secondTimestamp = LocalDateTime.of(2026, 9, 29, 16, 0);
    var firstEntity = AuditLogJpaEntity.builder()
      .auditLogId(1L)
      .operation("GET /product")
      .status("SUCCESS")
      .durationMs(25L)
      .time(firstTimestamp)
      .user("catalog-user")
      .build();
    var secondEntity = AuditLogJpaEntity.builder()
      .auditLogId(2L)
      .operation("POST /product-note")
      .status("FAILED")
      .durationMs(40L)
      .time(secondTimestamp)
      .user("catalog-user")
      .error("Note storage unavailable")
      .build();
    when(springDataAuditLogRepository.findAll()).thenReturn(List.of(firstEntity, secondEntity));

    List<AuditLog> auditLogs = adapter.findAll();

    assertEquals(2, auditLogs.size());
    assertEquals(1L, auditLogs.get(0).getAuditLogId());
    assertEquals("GET /product", auditLogs.get(0).getOperation());
    assertEquals("SUCCESS", auditLogs.get(0).getStatus());
    assertEquals(firstTimestamp, auditLogs.get(0).getTime());
    assertEquals(2L, auditLogs.get(1).getAuditLogId());
    assertEquals("POST /product-note", auditLogs.get(1).getOperation());
    assertEquals("FAILED", auditLogs.get(1).getStatus());
    assertEquals("Note storage unavailable", auditLogs.get(1).getError());
    verify(springDataAuditLogRepository).findAll();
  }


}
