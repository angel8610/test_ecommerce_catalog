package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.model.AuthRefreshToken;
import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.model.Login;
import com.example.ecommerce.catagol.application.port.in.*;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuthenticatedUserResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception.GlobalExceptionHandler;
import com.example.ecommerce.catagol.domain.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private static final String REFRESH_TOKEN = "12345abcde";

  @Mock
  private AuthenticateUserUseCase authenticateUserUseCase;
  @Mock
  private GenerateRefreshTokenUseCase generateRefreshTokenUseCase;
  @Mock
  private GetRefreshTokenUseCase getRefreshTokenUseCase;
  @Mock
  private LogoutUseCase logoutUseCase;

  private AuthController authController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authenticateUserUseCase, generateRefreshTokenUseCase,
      getRefreshTokenUseCase, logoutUseCase);
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
    var user = new AuthenticatedUser("username","username",
      Set.of("ROLE_ADMIN"), "firstName", "lastName");
    var login = new Login("signed-jwt", "Bearer", 900L, user);
    var userResponse = new AuthenticatedUserResponse(user.userId(), user.username(),
      user.roles(), user.firstName(), user.lastName());
    var loginResponse = new LoginResponse(login.accessToken(), login.tokenType(), login.expiresIn(),
      userResponse);

    when(authenticateUserUseCase.authenticate(loginCommand)).thenReturn(login);
    when(generateRefreshTokenUseCase.generateRefreshToken(loginRequest.username())).thenReturn(
      new AuthRefreshToken(null, REFRESH_TOKEN));

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
    var user = new AuthenticatedUser("username", "username",
      Set.of("ROLE_ADMIN", "ROLE_USER"), "firstName", "lastName");

    when(authenticateUserUseCase.authenticate(loginCommand))
      .thenReturn(new Login("signed-jwt", "Bearer", 900L, user));
    when(generateRefreshTokenUseCase.generateRefreshToken(loginRequest.username())).thenReturn(
      new AuthRefreshToken(null, REFRESH_TOKEN));

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
      .andExpect(jsonPath("$.expiresIn").value(900))
      .andExpect(jsonPath("$.user.userId").value("username"))
      .andExpect(jsonPath("$.user.username").value("username"))
      .andExpect(jsonPath("$.user.roles").isArray())
      .andExpect(jsonPath("$.user.roles").isNotEmpty())
      .andExpect(jsonPath("$.user.firstName").value("firstName"))
      .andExpect(jsonPath("$.user.lastName").value("lastName"));

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

  @Test
  void returnsUnauthorizedWhenRefreshCookieIsMissing() throws Exception {
    mockMvc.perform(post("/api/auth/refresh"))
      .andExpect(status().isUnauthorized())
      .andExpect(content().string(""));

    verifyNoInteractions(getRefreshTokenUseCase);
  }

  @Test
  void returnsNewAccessTokenAndRefreshCookieWhenRefreshSucceeds() throws Exception {
    when(getRefreshTokenUseCase.getRefreshToken(REFRESH_TOKEN))
      .thenReturn(new AuthRefreshToken("new-access-token", "rotated-refresh-token"));

    var response = mockMvc.perform(post("/api/auth/refresh")
        .cookie(new Cookie("refresh_token", REFRESH_TOKEN)))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(cookie().value("refresh_token", "rotated-refresh-token"))
      .andReturn()
      .getResponse();

    String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
    Assertions.assertNotNull(setCookie);
    assertTrue(setCookie.contains("refresh_token=rotated-refresh-token"));
    assertTrue(setCookie.contains("HttpOnly"));
    assertTrue(setCookie.contains("Secure")); // Add Secure
    assertTrue(setCookie.contains("SameSite=Lax")); // Change to Strict
    assertTrue(setCookie.contains("Path=/api/auth"));
    assertTrue(setCookie.contains("Max-Age=604800"));
    verify(getRefreshTokenUseCase).getRefreshToken(REFRESH_TOKEN);
  }

  @Test
  void returnsUnauthorizedWhenRefreshUseCaseRejectsToken() throws Exception {
    when(getRefreshTokenUseCase.getRefreshToken(REFRESH_TOKEN))
      .thenThrow(new UnauthorizedException("Token is expired"));

    mockMvc.perform(post("/api/auth/refresh")
        .cookie(new Cookie("refresh_token", REFRESH_TOKEN)))
      .andExpect(status().isUnauthorized())
      .andExpect(content().string("Token is expired"));

    verify(getRefreshTokenUseCase).getRefreshToken(REFRESH_TOKEN);
  }

  @Test
  void clearsRefreshCookieAndLogsOutWhenCookieIsPresent() throws Exception {
    var response = mockMvc.perform(post("/api/auth/logout")
        .cookie(new Cookie("refresh_token", REFRESH_TOKEN)))
      .andExpect(status().isNoContent())
      .andExpect(content().string(""))
      .andReturn()
      .getResponse();

    String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
    Assertions.assertNotNull(setCookie);
    assertTrue(setCookie.contains("refresh_token="));
    assertTrue(setCookie.contains("HttpOnly"));
    assertTrue(setCookie.contains("Secure")); // Add Secure
    assertTrue(setCookie.contains("SameSite=Lax")); // Change to Strict
    assertTrue(setCookie.contains("Path=/api/auth"));
    assertTrue(setCookie.contains("Max-Age=0"));
    verify(logoutUseCase).logout(REFRESH_TOKEN);
  }

  @Test
  void clearsRefreshCookieWithoutCallingLogoutWhenCookieIsMissing() throws Exception {
    var response = mockMvc.perform(post("/api/auth/logout"))
      .andExpect(status().isNoContent())
      .andExpect(content().string(""))
      .andReturn()
      .getResponse();

    String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
    Assertions.assertNotNull(setCookie);
    assertTrue(setCookie.contains("refresh_token="));
    assertTrue(setCookie.contains("Max-Age=0"));
    verifyNoInteractions(logoutUseCase);
  }


}
