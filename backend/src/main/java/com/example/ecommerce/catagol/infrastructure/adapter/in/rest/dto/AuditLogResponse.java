package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(

  Long auditLogId,
  String operation,
  String status,
  Long durationMs,
  LocalDateTime registerDate,
  String createdBy,
  String error


) {
}
