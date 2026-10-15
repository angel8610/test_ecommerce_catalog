package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import com.example.ecommerce.catagol.application.port.out.RefreshTokenGeneratorPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class JwtRefreshTokenAdapter implements RefreshTokenGeneratorPort {

  private static final int TOKEN_BYTES = 64;
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generateRawToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  @Override
  public String hash(String rawToken) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
        .digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not present", e);
    }
  }


}
