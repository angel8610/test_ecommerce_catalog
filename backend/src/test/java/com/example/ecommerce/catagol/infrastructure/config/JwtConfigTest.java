package com.example.ecommerce.catagol.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtConfigTest {

  private static final String ISSUER = "test-issuer";
  private static final String SECRET = Base64.getEncoder().encodeToString(
    "01234567890123456789012345678901".getBytes(java.nio.charset.StandardCharsets.UTF_8));

  private SecurityJwtConfig securityJwtConfig;
  private JwtConfig jwtConfig;

  @BeforeEach
  void setUp() {
    securityJwtConfig = new SecurityJwtConfig();
    securityJwtConfig.setIssuer(ISSUER);
    securityJwtConfig.setSecret(SECRET);
    jwtConfig = new JwtConfig(securityJwtConfig);
  }

  @Test
  void createsHmacSecretKeyFromBase64EncodedSecret() {
    SecretKey secretKey = jwtConfig.jwtSecretKey();

    assertEquals("HmacSHA256", secretKey.getAlgorithm());
    assertEquals("01234567890123456789012345678901",
      new String(secretKey.getEncoded(), java.nio.charset.StandardCharsets.UTF_8));
  }

  @Test
  void rejectsSecretShorterThan256Bits() {
    securityJwtConfig.setSecret(Base64.getEncoder().encodeToString(
      "short-secret".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

    var exception = assertThrows(IllegalArgumentException.class, jwtConfig::jwtSecretKey);

    assertEquals("JWT secret must contain at least 256 bits", exception.getMessage());
  }

  @Test
  void rejectsMalformedBase64Secret() {
    securityJwtConfig.setSecret("not-valid-base64!");

    assertThrows(IllegalArgumentException.class, jwtConfig::jwtSecretKey);
  }

  @Test
  void encodesAndDecodesSignedJwtWithHs256AndConfiguredIssuer() {
    SecretKey secretKey = jwtConfig.jwtSecretKey();
    JwtEncoder encoder = jwtConfig.jwtEncoder(secretKey);
    JwtDecoder decoder = jwtConfig.jwtDecoder(secretKey);
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(300);
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer(ISSUER)
      .subject("user-42")
      .issuedAt(issuedAt)
      .expiresAt(expiresAt)
      .claim("username", "jane.doe")
      .claim("roles", List.of("ROLE_USER", "ROLE_ADMIN"))
      .build();

    String token = encoder.encode(JwtEncoderParameters.from(
      JwsHeader.with(MacAlgorithm.HS256).keyId("jwt-key-1").build(), claims
    )).getTokenValue();

    Jwt decodedToken = decoder.decode(token);

    assertEquals(ISSUER, decodedToken.getClaimAsString("iss"));
    assertEquals("user-42", decodedToken.getSubject());
    assertEquals("jane.doe", decodedToken.getClaimAsString("username"));
    assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), decodedToken.getClaimAsStringList("roles"));
    assertEquals("HS256", decodedToken.getHeaders().get("alg"));
    assertEquals("jwt-key-1", decodedToken.getHeaders().get("kid"));
  }

  @Test
  void rejectsJwtWhenIssuerDoesNotMatchConfiguredIssuer() {
    SecretKey secretKey = jwtConfig.jwtSecretKey();
    JwtEncoder encoder = jwtConfig.jwtEncoder(secretKey);
    JwtDecoder decoder = jwtConfig.jwtDecoder(secretKey);
    Instant issuedAt = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer("different-issuer")
      .subject("user-42")
      .issuedAt(issuedAt)
      .expiresAt(issuedAt.plusSeconds(300))
      .build();
    String token = encoder.encode(JwtEncoderParameters.from(
      JwsHeader.with(MacAlgorithm.HS256).keyId("jwt-key-1").build(),
      claims)).getTokenValue();

    assertThrows(JwtException.class, () -> decoder.decode(token));
  }


}
