package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.model.Login;
import com.example.ecommerce.catagol.application.port.in.LoginCommand;
import com.example.ecommerce.catagol.application.port.out.TokenProviderPort;
import com.example.ecommerce.catagol.infrastructure.adapter.out.security.CustomUserDetails;
import com.example.ecommerce.catagol.infrastructure.config.SecurityJwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private TokenProviderPort tokenProvider;

  @Mock
  private SecurityJwtConfig securityJwtConfig;

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    authenticationService = new AuthenticationService(
      authenticationManager,
      tokenProvider,
      securityJwtConfig
    );
  }

  @Test
  void authenticatesUserAndReturnsBearerTokenWithConfiguredExpiration() {
    var request = new LoginCommand("loginUser", "correctPassword");
    var customUserDetails = buildCustomUserDetails(request.username(), request.password(),
      "ROLE_ADMIN");
    var user = new AuthenticatedUser(customUserDetails.getUsername(), customUserDetails.getUsername(),
      Set.of("ROLE_ADMIN"), customUserDetails.getFirstName(), customUserDetails.getLastName());
    var authentication = buildAuthenticatedUser(customUserDetails, "ROLE_ADMIN");

    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
    when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenReturn("signed-token");
    when(securityJwtConfig.getExpiration()).thenReturn(3600L);

    Login response = authenticationService.authenticate(request);

    assertEquals(new Login("signed-token", "Bearer", 3600L, user), response);

    var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(authenticationCaptor.capture());
    var submittedCredentials = authenticationCaptor.getValue();
    assertEquals("loginUser", submittedCredentials.getPrincipal());
    assertEquals("correctPassword", submittedCredentials.getCredentials());
    assertFalse(submittedCredentials.isAuthenticated());

    var userCaptor = ArgumentCaptor.forClass(AuthenticatedUser.class);
    verify(tokenProvider).generateToken(userCaptor.capture());
    assertEquals("loginUser", userCaptor.getValue().userId());
    assertEquals("loginUser", userCaptor.getValue().username());
    assertEquals(Set.of("ROLE_ADMIN"), userCaptor.getValue().roles());
    assertEquals("firstName", userCaptor.getValue().firstName());
    assertEquals("lastName", userCaptor.getValue().lastName());
  }

  @Test
  void createsAuthenticatedUserWithNoRolesWhenAuthenticationHasNoAuthorities() {
    var request = new LoginCommand("username", "username");
    var customUserDetails = buildCustomUserDetails(request.username(), request.password());
    var authentication = buildAuthenticatedUser(customUserDetails);
    var user = new AuthenticatedUser(customUserDetails.getUsername(), customUserDetails.getPassword(),
      Set.of(), "firstName", "lastName");

    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
    when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenReturn("signed-token");
    when(securityJwtConfig.getExpiration()).thenReturn(1200L);

    Login response = authenticationService.authenticate(request);

    assertEquals(new Login("signed-token", "Bearer", 1200L, user), response);
    var userCaptor = ArgumentCaptor.forClass(AuthenticatedUser.class);
    verify(tokenProvider).generateToken(userCaptor.capture());
    assertEquals(Set.of(), userCaptor.getValue().roles());
  }

  @Test
  void propagatesAuthenticationFailuresWithoutGeneratingToken() {
    var request = new LoginCommand("loginUser", "incorrectPassword");
    var authenticationException = new BadCredentialsException("Invalid credentials");

    when(authenticationManager.authenticate(any(Authentication.class)))
      .thenThrow(authenticationException);

    var exception = assertThrows(BadCredentialsException.class,
      () -> authenticationService.authenticate(request));

    assertSame(authenticationException, exception);
    verify(tokenProvider, never()).generateToken(any(AuthenticatedUser.class));
  }

  @Test
  void propagatesTokenGenerationFailuresAfterSuccessfulAuthentication() {
    var request = new LoginCommand("loginUser", "correctPassword");
    var customUserDetails = buildCustomUserDetails(request.username(), request.password(), "ROLE_USER");
    var authentication = buildAuthenticatedUser(customUserDetails, "ROLE_USER");
    var tokenException = new IllegalStateException("Token signing failed");

    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
    when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenThrow(tokenException);

    var exception = assertThrows(IllegalStateException.class,
      () -> authenticationService.authenticate(request));

    assertSame(tokenException, exception);
    verify(authenticationManager).authenticate(any(Authentication.class));
    verify(securityJwtConfig, never()).getExpiration();
  }

  private CustomUserDetails buildCustomUserDetails(String username, String password, String... roles) {
    var authorities = stream(roles)
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toSet());
    return new CustomUserDetails(username, password, authorities,
      "firstName","lastName");
  }

  private Authentication buildAuthenticatedUser(Object customUserDetails, String... roles) {
    return UsernamePasswordAuthenticationToken.authenticated(
      customUserDetails,
      null,
      stream(roles)
        .map(SimpleGrantedAuthority::new)
        .toList()
    );
  }


}
