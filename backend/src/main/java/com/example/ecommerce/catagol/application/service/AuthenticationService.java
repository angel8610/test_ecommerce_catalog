package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.model.Login;
import com.example.ecommerce.catagol.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.catagol.application.port.in.LoginCommand;
import com.example.ecommerce.catagol.application.port.out.TokenProviderPort;
import com.example.ecommerce.catagol.infrastructure.adapter.out.security.CustomUserDetails;
import com.example.ecommerce.catagol.infrastructure.config.SecurityJwtConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

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
  public Login authenticate(LoginCommand loginCommand) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(loginCommand.username(), loginCommand.password()));
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    Set<String> roles = authentication.getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toUnmodifiableSet());

    AuthenticatedUser user = new AuthenticatedUser(
      authentication.getName(),
      authentication.getName(),
      roles,
      userDetails.getFirstName(),
      userDetails.getLastName()
    );

    var token = tokenProvider.generateToken(user);

    return new Login(token, "Bearer", securityJwtConfig.getExpiration(), user);
  }


}
