package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class JwtRefreshTokenAdapterTest {

  private final JwtRefreshTokenAdapter adapter = new JwtRefreshTokenAdapter();

  @Test
  void generatesUrlSafeUnpaddedTokenContaining64RandomBytes() {
    String rawToken = adapter.generateRawToken();

    assertEquals(86, rawToken.length());
    assertTrue(rawToken.matches("[A-Za-z0-9_-]+"));
    assertEquals(64, Base64.getUrlDecoder().decode(rawToken).length);
  }

  @Test
  void generatesDifferentRawTokensAcrossCalls() {
    Set<String> rawTokens = new HashSet<>();

    for (int attempt = 0; attempt < 10; attempt++) {
      rawTokens.add(adapter.generateRawToken());
    }

    assertEquals(10, rawTokens.size());
  }

  @Test
  void hashesTokenUsingExpectedSha256HexadecimalValue() {
    String hash = adapter.hash("hello");

    assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", hash);
    assertTrue(hash.matches("[0-9a-f]{64}"));
  }

  @Test
  void producesSameHashForSameRawToken() {
    String firstHash = adapter.hash("refresh-token");
    String secondHash = adapter.hash("refresh-token");

    assertEquals(firstHash, secondHash);
  }

  @Test
  void producesDifferentHashesForDifferentRawTokens() {
    assertNotEquals(adapter.hash("first-token"), adapter.hash("second-token"));
  }

  @Test
  void hashesUnicodeTokenAsUtf8() {
    assertEquals("015dc06740d7aa10422fe34b0643a108286d6d90271077194fbbbdd2213fb6e3",
      adapter.hash("token-ñ-🔐"));
  }

  @Test
  void hashesEmptyToken() {
    assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      adapter.hash(""));
  }

  @Test
  void throwsNullPointerExceptionWhenHashingNullToken() {
    assertThrows(NullPointerException.class, () -> adapter.hash(null));
  }

  @Test
  void wrapsUnavailableSha256AlgorithmInIllegalStateException() {
    var algorithmException = new NoSuchAlgorithmException("SHA-256 unavailable");

    try(var messageDigestMock = mockStatic(MessageDigest.class)) {
      messageDigestMock.when(() -> MessageDigest.getInstance("SHA-256"))
        .thenThrow(algorithmException);

      var exception = assertThrows(IllegalStateException.class, () -> adapter.hash("refresh-token"));

      assertEquals("SHA-256 not present", exception.getMessage());
      assertSame(algorithmException, exception.getCause());
    }
  }

}
