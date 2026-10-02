package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.model.Login;
import com.example.ecommerce.catagol.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.catagol.application.port.in.LoginCommand;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthenticateUserUseCase authenticateUserUseCase;

  private AuthController authController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authenticateUserUseCase);
    var validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = standaloneSetup(authController)
      .setValidator(validator)
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }

  @Test
  void returnsOkWithLoginResponseWhenAuthenticationSucceeds() {
    var loginRequest = new LoginRequest("jane.doe", "correct-password");
    var loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());
    var login = new Login("signed-jwt", "Bearer", 900L);
    var loginResponse = new LoginResponse(login.accessToken(), login.tokenType(), login.expiresIn());
    when(authenticateUserUseCase.authenticate(loginCommand)).thenReturn(login);

    ResponseEntity<LoginResponse> response = authController.login(loginRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(loginResponse, response.getBody());
    verify(authenticateUserUseCase).authenticate(loginCommand);
  }

  @Test
  void propagatesAuthenticationErrorWhenCredentialsAreInvalid() {
    var loginRequest = new LoginRequest("jane.doe", "incorrect-password");
    var loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());
    var authenticationException = new BadCredentialsException("Invalid credentials");
    when(authenticateUserUseCase.authenticate(loginCommand)).thenThrow(authenticationException);

    var exception = assertThrows(BadCredentialsException.class,
      () -> authController.login(loginRequest));

    assertSame(authenticationException, exception);
    verify(authenticateUserUseCase).authenticate(loginCommand);
  }

  @Test
  void returnsTokenAsJsonWhenLoginEndpointReceivesValidRequest() throws Exception {
    var loginRequest = new LoginRequest("jane.doe", "correct-password");
    var loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());
    when(authenticateUserUseCase.authenticate(loginCommand))
      .thenReturn(new Login("signed-jwt", "Bearer", 900L));

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "username": "jane.doe",
            "password": "correct-password"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.accessToken").value("signed-jwt"))
      .andExpect(jsonPath("$.tokenType").value("Bearer"))
      .andExpect(jsonPath("$.expiresIn").value(900));

    verify(authenticateUserUseCase).authenticate(loginCommand);
  }

  @Test
  void returnsBadRequestWithoutCallingUseCaseWhenLoginRequestIsInvalid() throws Exception {
    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "username": "",
            "password": ""
          }
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Validation failed"))
      .andExpect(jsonPath("$.errors.length()").value(2))
      .andExpect(jsonPath("$.errors[?(@.field == 'username')]").isNotEmpty())
      .andExpect(jsonPath("$.errors[?(@.field == 'password')]").isNotEmpty());

    verifyNoInteractions(authenticateUserUseCase);
  }

  @Test
  void returnsNotFoundWhenLoginUseCaseRejectsUnknownUsername() throws Exception {
    when(authenticateUserUseCase.authenticate(any(LoginCommand.class)))
      .thenThrow(new UsernameNotFoundException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "username": "unknown",
            "password": "some-password"
          }
          """))
      .andExpect(status().isNotFound())
      .andExpect(content().string("Invalid credentials"));

    verify(authenticateUserUseCase).authenticate(
      new LoginCommand("unknown", "some-password"));
  }


}
