package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.AuditLogJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogPersistenceMapper {

  public AuditLogJpaEntity mapToEntity(AuditLog auditLog) {
    if(auditLog == null) {
      return null;
    }

    return AuditLogJpaEntity.builder()
      .createdBy(auditLog.getCreatedBy())
      .durationMs(auditLog.getDurationMs())
      .error(auditLog.getError())
      .operation(auditLog.getOperation())
      .status(auditLog.getStatus())
      .registerDate(auditLog.getRegisterDate())
      .build();
  }

  public AuditLog mapToDomain(AuditLogJpaEntity auditLogJpaEntity) {
    if(auditLogJpaEntity == null) {
      return null;
    }

    return AuditLog.builder()
      .auditLogId(auditLogJpaEntity.getAuditLogId())
      .createdBy(auditLogJpaEntity.getCreatedBy())
      .durationMs(auditLogJpaEntity.getDurationMs())
      .error(auditLogJpaEntity.getError())
      .operation(auditLogJpaEntity.getOperation())
      .registerDate(auditLogJpaEntity.getRegisterDate())
      .status(auditLogJpaEntity.getStatus())
      .build();
  }


}
