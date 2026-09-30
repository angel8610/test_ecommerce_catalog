package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuditLogResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

  @Mock
  private AuditLogRepositoryPort auditLogRepositoryPort;

  private AuditLogService auditLogService;

  @BeforeEach
  void setUp() {
    auditLogService = new AuditLogService(auditLogRepositoryPort);
  }

  @Test
  void returnsAllAuditLogsMappedToResponsesInRepositoryOrder() {
    var firstTime = LocalDateTime.of(2026, 9, 29, 22, 0);
    var secondTime = LocalDateTime.of(2026, 9, 29, 22, 1);
    var firstAuditLog = AuditLog.builder()
      .auditLogId(1L)
      .operation("GET PRODUCTS API")
      .status("SUCCESS")
      .durationMs(42L)
      .registerDate(firstTime)
      .createdBy("API")
      .build();
    var secondAuditLog = AuditLog.builder()
      .auditLogId(2L)
      .operation("SAVE PRODUCT NOTE")
      .status("FAILED")
      .durationMs(75L)
      .registerDate(secondTime)
      .createdBy("REGISTER")
      .error("Error saving product note")
      .build();
    when(auditLogRepositoryPort.findAll())
      .thenReturn(List.of(firstAuditLog, secondAuditLog));

    List<AuditLogResponse> responses = auditLogService.findAll();

    assertEquals(List.of(
      new AuditLogResponse(1L, "GET PRODUCTS API", "SUCCESS", 42L, firstTime, "API", null),
      new AuditLogResponse(2L, "SAVE PRODUCT NOTE", "FAILED", 75L, secondTime,
        "REGISTER", "Error saving product note")
    ), responses);
    verify(auditLogRepositoryPort).findAll();
  }

  @Test
  void returnsEmptyListWhenRepositoryHasNoAuditLogs() {
    when(auditLogRepositoryPort.findAll()).thenReturn(List.of());

    List<AuditLogResponse> responses = auditLogService.findAll();

    assertEquals(List.of(), responses);
    verify(auditLogRepositoryPort).findAll();
  }
}