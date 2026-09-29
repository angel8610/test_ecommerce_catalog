package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.AuditLogPersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpringDataAuditLogRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuditLogRepositoryAdapter implements AuditLogRepositoryPort {

  private final SpringDataAuditLogRepository springDataAuditLogRepository;
  private final AuditLogPersistenceMapper auditLogPersistenceMapper;

  public JpaAuditLogRepositoryAdapter(SpringDataAuditLogRepository springDataAuditLogRepository,
                                      AuditLogPersistenceMapper auditLogPersistenceMapper) {
    this.springDataAuditLogRepository = springDataAuditLogRepository;
    this.auditLogPersistenceMapper = auditLogPersistenceMapper;
  }

  @Override
  public AuditLog save(AuditLog auditLog) {
    var auditLogEntity = this.springDataAuditLogRepository.save(
      this.auditLogPersistenceMapper.mapToEntity(auditLog));
    return this.auditLogPersistenceMapper.mapToDomain(auditLogEntity);
  }


}
