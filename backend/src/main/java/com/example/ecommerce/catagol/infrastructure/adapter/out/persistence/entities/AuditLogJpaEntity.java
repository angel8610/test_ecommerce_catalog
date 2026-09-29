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

  @Column(name = "endpoint_url", nullable = false)
  private String endpointUrl;

  @Column(name = "http_method", nullable = false, length = 10)
  private String httpMethod;

  @Column(name = "status_response", nullable = false, length = 20)
  private String statusResponse;

  @Column(name = "response_time_ms", nullable = false)
  private Long responseTimeMs;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  @PrePersist
  protected void onCreate() {
    this.timestamp = LocalDateTime.now();
  }


}
