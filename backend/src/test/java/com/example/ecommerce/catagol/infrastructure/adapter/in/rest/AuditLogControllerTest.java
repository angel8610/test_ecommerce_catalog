package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.FindAllAuditLogUseCase;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuditLogResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

  @Mock
  private FindAllAuditLogUseCase findAllAuditLogUseCase;

  private AuditLogController auditLogController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    auditLogController = new AuditLogController(findAllAuditLogUseCase);
    mockMvc = standaloneSetup(auditLogController).build();
  }

  @Test
  void returnsOkWithAllAuditLogs() {
    var auditLogs = List.of(
      new AuditLogResponse(1L, "GET PRODUCTS API", "SUCCESS", 42L,
        LocalDateTime.of(2026, 9, 29, 22, 0), "API", null),
      new AuditLogResponse(2L, "SAVE PRODUCT NOTE", "FAILED", 75L,
        LocalDateTime.of(2026, 9, 29, 22, 1), "REGISTER", "Error saving product note")
    );
    when(findAllAuditLogUseCase.findAll()).thenReturn(auditLogs);

    ResponseEntity<List<AuditLogResponse>> response = auditLogController.findAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(auditLogs, response.getBody());
    verify(findAllAuditLogUseCase).findAll();
  }

  @Test
  void returnsOkWithEmptyListWhenNoAuditLogsExist() {
    when(findAllAuditLogUseCase.findAll()).thenReturn(List.of());

    ResponseEntity<List<AuditLogResponse>> response = auditLogController.findAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(List.of(), response.getBody());
    verify(findAllAuditLogUseCase).findAll();
  }

  @Test
  void propagatesUseCaseExceptions() {
    var useCaseException = new IllegalStateException("Audit log service unavailable");
    when(findAllAuditLogUseCase.findAll()).thenThrow(useCaseException);

    var exception = assertThrows(IllegalStateException.class, auditLogController::findAll);

    assertSame(useCaseException, exception);
    verify(findAllAuditLogUseCase).findAll();
  }

  @Test
  void returnsAuditLogsAsJsonForGetRequest() throws Exception {
    when(findAllAuditLogUseCase.findAll()).thenReturn(List.of(
      new AuditLogResponse(1L, "GET PRODUCTS API", "SUCCESS", 42L,
        LocalDateTime.of(2026, 9, 29, 22, 0), "API", null)
    ));

    mockMvc.perform(get("/api/v1/auditlogs"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].auditLogId").value(1))
      .andExpect(jsonPath("$[0].operation").value("GET PRODUCTS API"))
      .andExpect(jsonPath("$[0].status").value("SUCCESS"))
      .andExpect(jsonPath("$[0].durationMs").value(42))
      .andExpect(jsonPath("$[0].createdBy").value("API"))
      .andExpect(jsonPath("$[0].error").doesNotExist());

    verify(findAllAuditLogUseCase).findAll();
  }

}