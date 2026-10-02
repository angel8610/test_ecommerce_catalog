package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.port.out.TokenProviderPort;
import com.example.ecommerce.catagol.infrastructure.config.SecurityJwtConfig;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenAdapter implements TokenProviderPort {

  private final JwtEncoder jwtEncoder;
  private final SecurityJwtConfig securityJwtConfig;

  public JwtTokenAdapter(JwtEncoder jwtEncoder, SecurityJwtConfig securityJwtConfig) {
    this.jwtEncoder = jwtEncoder;
    this.securityJwtConfig = securityJwtConfig;
  }

  @Override
  public String generateToken(AuthenticatedUser user) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer(securityJwtConfig.getIssuer())
      .subject(user.userId())
      .issuedAt(now)
      .expiresAt(now.plusSeconds(securityJwtConfig.getExpiration()))
      .claim("username", user.username())
      .claim("roles", user.roles())
      .build();

    JwsHeader header = JwsHeader
      .with(MacAlgorithm.HS256)
      .keyId("jwt-key-1")
      .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
      .getTokenValue();
  }


}
