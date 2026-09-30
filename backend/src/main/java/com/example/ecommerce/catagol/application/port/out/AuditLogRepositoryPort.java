package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.domain.model.AuditLog;

import java.util.List;

public interface AuditLogRepositoryPort {

  AuditLog save(AuditLog auditLog);

  List<AuditLog> findAll();


}
