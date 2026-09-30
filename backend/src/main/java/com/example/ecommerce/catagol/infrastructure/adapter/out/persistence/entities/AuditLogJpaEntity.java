package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long auditLogId;

  @Column(name = "operation_type", nullable = false, length = 100)
  private String operation;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "duration_ms", nullable = false)
  private Long durationMs;

  @Column(name = "regis_date", nullable = false)
  private LocalDateTime registerDate;

  @Column(name = "created_by", nullable = false, length = 50)
  private String createdBy;

  @Column(name = "error", length = 500)
  private String error;

  @PrePersist
  protected void onCreate() {
    this.registerDate = LocalDateTime.now();
  }


}
