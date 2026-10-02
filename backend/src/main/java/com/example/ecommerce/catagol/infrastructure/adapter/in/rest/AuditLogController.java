package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.FindAllAuditLogUseCase;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuditLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditlogs")
public class AuditLogController {

  private final FindAllAuditLogUseCase findAllAuditLogUseCase;

  public AuditLogController(FindAllAuditLogUseCase findAllAuditLogUseCase) {
    this.findAllAuditLogUseCase = findAllAuditLogUseCase;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ResponseEntity<List<AuditLogResponse>> findAll() {
    var auditLogs = this.findAllAuditLogUseCase.findAll();
    var auditLogResponses = auditLogs.stream()
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
    return ResponseEntity.ok(auditLogResponses);
  }


}
