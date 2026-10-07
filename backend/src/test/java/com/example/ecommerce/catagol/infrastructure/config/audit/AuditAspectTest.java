package com.example.ecommerce.catagol.infrastructure.config.audit;

import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.AuthenticationPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

  @Mock
  private AuthenticationPort authenticationPort;

  @Mock
  private AuditLogRepositoryPort auditLogRepositoryPort;

  @Mock
  private ProceedingJoinPoint joinPoint;

  @Mock
  private Auditable auditable;

  private AuditAspect auditAspect;

  @BeforeEach
  void setUp() {
    auditAspect = new AuditAspect(authenticationPort, auditLogRepositoryPort);
    when(auditable.operation()).thenReturn("CREATE_PRODUCT_NOTE");
  }

  @Test
  void returnsProceedResultAndSavesSuccessfulAuditLog() throws Throwable {
    var result = new Object();
    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.of("jane.doe"));
    when(joinPoint.proceed()).thenReturn(result);
    stubAuditLogSave();

    Object actualResult = auditAspect.logAuditAction(joinPoint, auditable);

    assertSame(result, actualResult);
    var auditLog = capturedAuditLog();
    assertEquals("CREATE_PRODUCT_NOTE", auditLog.getOperation());
    assertEquals("SUCCESS", auditLog.getStatus());
    assertEquals("jane.doe", auditLog.getCreatedBy());
    assertNull(auditLog.getError());
    assertTrue(auditLog.getDurationMs() >= 0);
    assertNotNull(auditLog.getRegisterDate());
    assertTrue(auditLog.getRegisterDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    verify(joinPoint).proceed();
  }

  @Test
  void savesSuccessfulAuditLogWithoutUsernameWhenNoUserIsAuthenticated() throws Throwable {
    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.empty());
    when(joinPoint.proceed()).thenReturn("completed");
    stubAuditLogSave();

    Object result = auditAspect.logAuditAction(joinPoint, auditable);

    assertEquals("completed", result);
    var auditLog = capturedAuditLog();
    assertEquals("SUCCESS", auditLog.getStatus());
    assertNull(auditLog.getCreatedBy());
    assertNull(auditLog.getError());
  }

  @Test
  void savesFailedAuditLogAndRethrowsProceedException() throws Throwable {
    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.of("jane.doe"));
    var proceedException = new IllegalStateException("Operation failed");
    when(joinPoint.proceed()).thenThrow(proceedException);
    stubAuditLogSave();

    var exception = assertThrows(IllegalStateException.class,
      () -> auditAspect.logAuditAction(joinPoint, auditable));

    assertSame(proceedException, exception);
    var auditLog = capturedAuditLog();
    assertEquals("CREATE_PRODUCT_NOTE", auditLog.getOperation());
    assertEquals("FAILED", auditLog.getStatus());
    assertEquals("jane.doe", auditLog.getCreatedBy());
    assertEquals("Operation failed", auditLog.getError());
    assertTrue(auditLog.getDurationMs() >= 0);
    verify(joinPoint).proceed();
  }

  @Test
  void propagatesAuthenticationLookupExceptionWithoutProceedingOrSavingAuditLog() {
    var authenticationException = new IllegalStateException("Authentication unavailable");
    when(authenticationPort.getAuthenticatedUsername()).thenThrow(authenticationException);

    var exception = assertThrows(IllegalStateException.class,
      () -> auditAspect.logAuditAction(joinPoint, auditable));

    assertSame(authenticationException, exception);
    verifyNoInteractions(auditLogRepositoryPort);
    verifyNoInteractions(joinPoint);
  }

  @Test
  void propagatesAuditRepositoryExceptionAfterProceeding() throws Throwable {
    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.of("jane.doe"));
    when(joinPoint.proceed()).thenReturn("completed");
    var repositoryException = new IllegalStateException("Audit repository unavailable");
    when(auditLogRepositoryPort.save(any(AuditLog.class))).thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class,
      () -> auditAspect.logAuditAction(joinPoint, auditable));

    assertSame(repositoryException, exception);
    verify(joinPoint).proceed();
    verify(auditLogRepositoryPort).save(any(AuditLog.class));
  }

  private AuditLog capturedAuditLog() {
    var auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepositoryPort).save(auditLogCaptor.capture());
    return auditLogCaptor.getValue();
  }

  private void stubAuditLogSave() {
    when(auditLogRepositoryPort.save(any(AuditLog.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));
  }


}
