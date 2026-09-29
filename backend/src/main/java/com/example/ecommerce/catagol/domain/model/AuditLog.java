package com.example.ecommerce.catagol.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditLog {

  @EqualsAndHashCode.Include
  private Long auditLogId;

  private String endpointUrl;

  private String httpMethod;

  private String statusResponse;

  private Long responseTimeMs;

  private LocalDateTime timestamp;

  private String errorMessage;


}
