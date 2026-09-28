package com.example.ecommerce.catagol.infrastructure.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

  private final SecurityJwtConfig securityJwtConfig;

  public JwtConfig(SecurityJwtConfig securityJwtConfig) {
    this.securityJwtConfig = securityJwtConfig;
  }

  @Bean
  public SecretKey jwtSecretKey() {
    byte[] keyBytes = Base64.getDecoder().decode(securityJwtConfig.getSecret());

    if (keyBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret must contain at least 256 bits");
    }

    return new SecretKeySpec(keyBytes, "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
    OctetSequenceKey jwk = new OctetSequenceKey.Builder(jwtSecretKey)
      .keyID("jwt-key-1")
      .keyUse(KeyUse.SIGNATURE)
      .algorithm(JWSAlgorithm.HS256)
      .build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
      .withSecretKey(jwtSecretKey)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(securityJwtConfig.getIssuer()));

    return decoder;
  }


}
