package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.model.AuthRefreshToken;
import com.example.ecommerce.catagol.application.port.in.*;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AccessTokenResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.AuthenticatedUserResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticateUserUseCase authenticateUserUseCase;
  private final GenerateRefreshTokenUseCase generateRefreshTokenUseCase;
  private final GetRefreshTokenUseCase getRefreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;

  public AuthController(AuthenticateUserUseCase authenticateUserUseCase,
                        GenerateRefreshTokenUseCase generateRefreshTokenUseCase,
                        GetRefreshTokenUseCase getRefreshTokenUseCase,
                        LogoutUseCase logoutUseCase) {
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.generateRefreshTokenUseCase = generateRefreshTokenUseCase;
    this.getRefreshTokenUseCase = getRefreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    var loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());
    var login = authenticateUserUseCase.authenticate(loginCommand);
    var refreshToken = this.generateRefreshTokenUseCase.generateRefreshToken(loginRequest.username());

    var user = login.user();
    var userResponse = new AuthenticatedUserResponse(
      user.userId(),
      user.username(),
      user.roles(),
      user.firstName(),
      user.lastName()
    );
    var loginResponse = new LoginResponse(
      login.accessToken(),
      login.tokenType(),
      login.expiresIn(),
      refreshToken.refreshToken(),
      userResponse
    );
    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AccessTokenResponse> refresh(
    @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    if(refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    AuthRefreshToken token = this.getRefreshTokenUseCase.getRefreshToken(refreshToken);

    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, refreshCookie(token.refreshToken(), Duration.ofDays(7)).toString())
      .body(new AccessTokenResponse(token.accessToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
    @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    if(refreshToken != null) {
      this.logoutUseCase.logout(refreshToken);
    }
    return ResponseEntity.noContent()
      .header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString())
      .build();
  }

  private ResponseCookie refreshCookie(String value, Duration maxAge) {
    return ResponseCookie.from("refreshToken", value)
      .httpOnly(true)
      .secure(true)
      .sameSite("Strict")
      .path("/api/auth")
      .maxAge(maxAge)
      .build();
  }


}
