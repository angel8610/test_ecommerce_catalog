package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.catagol.application.port.out.TokenProviderPort;
import com.example.ecommerce.catagol.domain.model.AuthenticatedUser;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginResponse;
import com.example.ecommerce.catagol.infrastructure.config.SecurityJwtConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService implements AuthenticateUserUseCase {

  private final AuthenticationManager authenticationManager;
  private final TokenProviderPort tokenProvider;
  private final SecurityJwtConfig securityJwtConfig;

  public AuthenticationService(AuthenticationManager authenticationManager,
    TokenProviderPort tokenProvider, SecurityJwtConfig securityJwtConfig) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
    this.securityJwtConfig = securityJwtConfig;
  }

  @Override
  public LoginResponse authenticate(LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
    Set<String> roles = authentication.getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toUnmodifiableSet());

    AuthenticatedUser user = new AuthenticatedUser(
      authentication.getName(),
      authentication.getName(),
      roles
    );

    var token = tokenProvider.generateToken(user);
    return new LoginResponse(token, "Bearer", securityJwtConfig.getExpiration());
  }


}
