package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.FindAllAuditLogUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuditLogResponse;

import java.util.List;

public class AuditLogService implements FindAllAuditLogUseCase {

  private final AuditLogRepositoryPort auditLogRepositoryPort;

  public AuditLogService(AuditLogRepositoryPort auditLogRepositoryPort) {
    this.auditLogRepositoryPort = auditLogRepositoryPort;
  }

  @Override
  public List<AuditLogResponse> findAll() {
    List<AuditLog> auditLogs = this.auditLogRepositoryPort.findAll();
    return auditLogs.stream()
      .map(auditLog -> new AuditLogResponse(
        auditLog.getAuditLogId(),
        auditLog.getOperation(),
        auditLog.getStatus(),
        auditLog.getDurationMs(),
        auditLog.getRegisterDate(),
        auditLog.getCreatedBy(),
        auditLog.getError()
      ))
      .toList();
  }


}
