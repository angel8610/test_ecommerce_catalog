package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.domain.model.AuditLog;

import java.util.List;

public interface FindAllAuditLogUseCase {

  List<AuditLog> findAll();


}
