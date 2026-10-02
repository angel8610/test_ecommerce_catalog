package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.catagol.application.port.in.LoginCommand;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticateUserUseCase authenticateUserUseCase;

  public AuthController(AuthenticateUserUseCase authenticateUserUseCase) {
    this.authenticateUserUseCase = authenticateUserUseCase;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    var loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());
    var login = authenticateUserUseCase.authenticate(loginCommand);

    var loginResponse = new LoginResponse(
      login.accessToken(),
      login.tokenType(),
      login.expiresIn()
    );
    return ResponseEntity.ok(loginResponse);
  }


}
