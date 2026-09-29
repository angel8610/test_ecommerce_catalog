package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.domain.model.AuditLog;

public interface AuditLogRepositoryPort {

  AuditLog save(AuditLog auditLog);


}
