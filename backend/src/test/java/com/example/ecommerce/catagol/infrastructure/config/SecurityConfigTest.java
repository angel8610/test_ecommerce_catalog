package com.example.ecommerce.catagol.infrastructure.config;

import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
  controllers = TestController.class,
  properties = "external.api.fake-store.url=http://localhost:8080"
)
@Import(SecurityConfig.class)
class SecurityConfigTest {

  private final SecurityConfig securityConfig = new SecurityConfig();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @Test
  void allowsRequestsToPublicTestEndpointWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/test"))
      .andExpect(status().isOk());
  }

  @Test
  void allowsRequestsToAuthenticationPathsWithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/auth/unknown"))
      .andExpect(status().isNotFound());
  }

  @Test
  void requiresAuthenticationForOtherEndpoints() throws Exception {
    mockMvc.perform(get("/api/v1/private"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void encodesPasswordsAndMatchesOnlyTheOriginalPassword() {
    String encodedPassword = securityConfig.passwordEncoder().encode("correct-password");

    assertNotNull(encodedPassword);
    assertFalse(encodedPassword.isBlank());
    assertTrue(passwordEncoder.matches("correct-password", encodedPassword));
    assertFalse(passwordEncoder.matches("incorrect-password", encodedPassword));
  }

  @Test
  void authenticatesValidCredentialsThroughConfiguredAuthenticationProvider() {
    UserDetails userDetails = User.withUsername("jane.doe")
      .password(passwordEncoder.encode("correct-password"))
      .authorities("ROLE_USER")
      .build();
    when(userDetailsService.loadUserByUsername("jane.doe")).thenReturn(userDetails);
    var authenticationProvider = securityConfig.authenticationProvider(
      userDetailsService, passwordEncoder);

    var authentication = authenticationProvider.authenticate(
      new UsernamePasswordAuthenticationToken("jane.doe", "correct-password"));

    assertTrue(authentication.isAuthenticated());
    assertEquals("jane.doe", authentication.getName());
    assertEquals(List.of(new SimpleGrantedAuthority("ROLE_USER")),
      List.copyOf(authentication.getAuthorities()));
  }

  @Test
  void rejectsInvalidCredentialsThroughConfiguredAuthenticationProvider() {
    UserDetails userDetails = User.withUsername("jane.doe")
      .password(passwordEncoder.encode("correct-password"))
      .authorities("ROLE_USER")
      .build();
    when(userDetailsService.loadUserByUsername("jane.doe")).thenReturn(userDetails);
    var authenticationProvider = securityConfig.authenticationProvider(
      userDetailsService, passwordEncoder);

    assertThrows(BadCredentialsException.class,
      () -> authenticationProvider.authenticate(
        new UsernamePasswordAuthenticationToken("jane.doe", "incorrect-password")
      )
    );
  }

  @Test
  void authenticatesCredentialsThroughConfiguredAuthenticationManager() {
    UserDetails userDetails = User.withUsername("jane.doe")
      .password(passwordEncoder.encode("correct-password"))
      .authorities("ROLE_USER")
      .build();
    when(userDetailsService.loadUserByUsername("jane.doe")).thenReturn(userDetails);
    var provider = securityConfig.authenticationProvider(userDetailsService, passwordEncoder);
    AuthenticationManager authenticationManager = securityConfig.authenticationManager(provider);

    var authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken("jane.doe", "correct-password"));

    assertTrue(authentication.isAuthenticated());
    assertEquals("jane.doe", authentication.getName());
  }

  @Test
  void convertsJwtRolesIntoAuthoritiesWithoutAddingPrefix() {
    Instant now = Instant.now();
    var jwt = Jwt.withTokenValue("test-token")
      .header("alg", "HS256")
      .subject("user-42")
      .issuedAt(now)
      .expiresAt(now.plusSeconds(300))
      .claim("roles", List.of("ROLE_ADMIN", "ROLE_USER"))
      .build();

    AbstractAuthenticationToken authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

    assertNotNull(authentication);
    assertEquals("user-42", authentication.getName());
    assertEquals(
      List.of(
        new SimpleGrantedAuthority("ROLE_ADMIN"),
        new SimpleGrantedAuthority("ROLE_USER")
      ),
      List.copyOf(authentication.getAuthorities())
    );
  }

  @Test
  void configuresCorsForTheFrontendOriginAndExpectedMethods() {
    CorsConfigurationSource source = securityConfig.corsConfigurationSource();
    CorsConfiguration configuration = source.getCorsConfiguration(
      new MockHttpServletRequest("GET", "/api/v1/products/all")
    );

    assertNotNull(configuration);
    assertEquals(List.of("http://localhost:4200"), configuration.getAllowedOrigins());
    assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
      configuration.getAllowedMethods());
    assertEquals(List.of("*"), configuration.getAllowedHeaders());
    assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
  }


}
