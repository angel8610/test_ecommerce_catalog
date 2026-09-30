package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuditLogResponse;

import java.util.List;

public interface FindAllAuditLogUseCase {

  List<AuditLogResponse> findAll();


}
