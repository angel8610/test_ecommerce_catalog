package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import com.example.ecommerce.catagol.domain.model.AuthenticatedUser;
import com.example.ecommerce.catagol.infrastructure.config.SecurityJwtConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenAdapterTest {

  @Mock
  private JwtEncoder jwtEncoder;

  private JwtTokenAdapter adapter;

  @BeforeEach
  void setUp() {
    SecurityJwtConfig securityJwtConfig = new SecurityJwtConfig();
    securityJwtConfig.setIssuer("test-issuer");
    securityJwtConfig.setExpiration(900L);
    adapter = new JwtTokenAdapter(jwtEncoder, securityJwtConfig);
  }

  @Test
  void generatesTokenWithExpectedHeaderAndClaims() {
    var user = new AuthenticatedUser("user-42", "jane.doe", Set.of("ROLE_ADMIN", "ROLE_USER"));
    var jwt = mock(Jwt.class);
    when(jwt.getTokenValue()).thenReturn("signed-jwt");
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
    Instant startedAt = Instant.now();

    String token = adapter.generateToken(user);

    Instant finishedAt = Instant.now();
    assertEquals("signed-jwt", token);

    var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());
    var parameters = parametersCaptor.getValue();
    var header = parameters.getJwsHeader();
    var claims = parameters.getClaims();

    Assertions.assertNotNull(header);
    assertEquals("HS256", header.getHeaders().get("alg").toString());
    assertEquals("jwt-key-1", header.getKeyId());
    assertEquals("test-issuer", claims.getClaim("iss"));
    assertEquals("user-42", claims.getSubject());
    assertEquals("jane.doe", claims.getClaim("username"));
    assertEquals(Set.of("ROLE_ADMIN", "ROLE_USER"), claims.getClaim("roles"));
    assertEquals(900L, claims.getExpiresAt().getEpochSecond() - claims.getIssuedAt().getEpochSecond());
    assertTrue(claims.getIssuedAt().compareTo(startedAt) >= 0);
    assertTrue(claims.getIssuedAt().compareTo(finishedAt) <= 0);
  }

  @Test
  void generatesTokenWithEmptyRolesWhenUserHasNoRoles() {
    var user = new AuthenticatedUser("user-42", "jane.doe", Set.of());
    var jwt = mock(Jwt.class);
    when(jwt.getTokenValue()).thenReturn("signed-jwt");
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

    String token = adapter.generateToken(user);

    assertEquals("signed-jwt", token);
    var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());
    assertEquals(Set.of(), parametersCaptor.getValue().getClaims().getClaim("roles"));
  }

  @Test
  void propagatesJwtEncoderErrorsWhenGeneratingToken() {
    var user = new AuthenticatedUser("user-42", "jane.doe", Set.of("ROLE_USER"));
    var encoderException = new IllegalStateException("JWT signing failed");
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenThrow(encoderException);

    var exception = assertThrows(
      IllegalStateException.class,
      () -> adapter.generateToken(user)
    );

    assertSame(encoderException, exception);
  }


}
