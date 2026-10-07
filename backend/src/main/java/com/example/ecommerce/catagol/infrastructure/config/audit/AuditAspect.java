package com.example.ecommerce.catagol.infrastructure.config.audit;

import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.AuthenticationPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

  private final AuditLogRepositoryPort auditLogRepositoryPort;
  private final AuthenticationPort authenticationPort;

  public AuditAspect(AuthenticationPort authenticationPort,
                     AuditLogRepositoryPort auditLogRepositoryPort) {
    this.authenticationPort = authenticationPort;
    this.auditLogRepositoryPort = auditLogRepositoryPort;

  }

  @Around("@annotation(auditable)")
  public Object logAuditAction(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
    String status = "SUCCESS";
    String error = null;
    String operation = auditable.operation();
    String currentUser = this.authenticationPort.getAuthenticatedUsername().orElse(null);

    Object result;
    long startTime = System.currentTimeMillis();
    try {
      result = joinPoint.proceed();
      return result;
    } catch (Throwable throwable) {
      status = "FAILED";
      error = throwable.getMessage();

      throw throwable;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      var errorLog = AuditLog.builder()
       .operation(operation)
       .durationMs(duration)
       .error(error)
       .registerDate(LocalDateTime.now())
       .status(status)
       .createdBy(currentUser)
       .build();
      this.auditLogRepositoryPort.save(errorLog);
    }
  }


}
