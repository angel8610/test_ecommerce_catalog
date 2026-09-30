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

  private String operation;

  private String status;

  private Long durationMs;

  private LocalDateTime registerDate;

  private String createdBy;

  private String error;


}
