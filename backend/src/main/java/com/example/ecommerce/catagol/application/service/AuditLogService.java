package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.FindAllAuditLogUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;

import java.util.List;

public class AuditLogService implements FindAllAuditLogUseCase {

  private final AuditLogRepositoryPort auditLogRepositoryPort;

  public AuditLogService(AuditLogRepositoryPort auditLogRepositoryPort) {
    this.auditLogRepositoryPort = auditLogRepositoryPort;
  }

  @Override
  public List<AuditLog> findAll() {
    return this.auditLogRepositoryPort.findAll();
  }


}
