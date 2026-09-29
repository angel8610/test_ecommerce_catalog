package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories;

import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpaEntity, Long> {


}
