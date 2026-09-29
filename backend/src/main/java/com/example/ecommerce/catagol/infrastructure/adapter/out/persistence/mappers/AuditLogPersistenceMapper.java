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
      .endpointUrl(auditLog.getEndpointUrl())
      .errorMessage(auditLog.getErrorMessage())
      .httpMethod(auditLog.getHttpMethod())
      .responseTimeMs(auditLog.getResponseTimeMs())
      .statusResponse(auditLog.getStatusResponse())
      .timestamp(auditLog.getTimestamp())
      .build();
  }

  public AuditLog mapToDomain(AuditLogJpaEntity auditLogJpaEntity) {
    if(auditLogJpaEntity == null) {
      return null;
    }

    return AuditLog.builder()
      .auditLogId(auditLogJpaEntity.getAuditLogId())
      .endpointUrl(auditLogJpaEntity.getEndpointUrl())
      .errorMessage(auditLogJpaEntity.getErrorMessage())
      .httpMethod(auditLogJpaEntity.getHttpMethod())
      .responseTimeMs(auditLogJpaEntity.getResponseTimeMs())
      .statusResponse(auditLogJpaEntity.getStatusResponse())
      .timestamp(auditLogJpaEntity.getTimestamp())
      .build();
  }


}
