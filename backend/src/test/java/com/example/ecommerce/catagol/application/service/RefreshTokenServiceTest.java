package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.AuthRefreshToken;
import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.port.out.RefreshTokenGeneratorPort;
import com.example.ecommerce.catagol.application.port.out.RefreshTokenPort;
import com.example.ecommerce.catagol.application.port.out.TokenProviderPort;
import com.example.ecommerce.catagol.application.port.out.UserRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.exception.UnauthorizedException;
import com.example.ecommerce.catagol.domain.model.RefreshToken;
import com.example.ecommerce.catagol.domain.model.Role;
import com.example.ecommerce.catagol.domain.model.User;
import com.example.ecommerce.catagol.infrastructure.config.SecurityRefreshConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  private static final Instant NOW = Instant.parse("2026-10-14T18:00:00Z");
  private static final Long USER_ID = 42L;
  private static final UUID FAMILY_ID = UUID.fromString("17f03ad8-0c30-497a-8f01-28bd847d93b9");

  @Mock
  private RefreshTokenPort refreshTokenPort;

  @Mock
  private RefreshTokenGeneratorPort refreshTokenGeneratorPort;

  @Mock
  private UserRepositoryPort userRepositoryPort;

  @Mock
  private TokenProviderPort tokenProviderPort;

  private RefreshTokenService refreshTokenService;

  @BeforeEach
  void setUp() {
    SecurityRefreshConfig securityRefreshConfig = new SecurityRefreshConfig();
    securityRefreshConfig.setExpirationDays(7L);
    refreshTokenService = new RefreshTokenService(
      Clock.fixed(NOW, ZoneOffset.UTC),
      refreshTokenPort,
      refreshTokenGeneratorPort,
      userRepositoryPort,
      tokenProviderPort,
      securityRefreshConfig
    );
  }

  @Test
  void generatesAndSavesRefreshTokenForExistingUser() {
    var user = createUser(List.of());
    when(userRepositoryPort.findByUsername("jane.doe")).thenReturn(Optional.of(user));
    when(refreshTokenGeneratorPort.generateRawToken()).thenReturn("raw-refresh-token");
    when(refreshTokenGeneratorPort.hash("raw-refresh-token"))
      .thenReturn("hashed-refresh-token");

    AuthRefreshToken result = refreshTokenService.generateRefreshToken("jane.doe");

    assertEquals(new AuthRefreshToken(null, "raw-refresh-token"), result);
    var tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenPort).save(tokenCaptor.capture());
    RefreshToken savedToken = tokenCaptor.getValue();
    assertNotNull(savedToken.getId());
    assertEquals(USER_ID, savedToken.getUserId());
    assertNotNull(savedToken.getFamilyId());
    assertEquals("hashed-refresh-token", savedToken.getTokenHash());
    assertEquals(NOW.plusSeconds(7 * 24 * 60 * 60L), savedToken.getExpiresAt());
    assertNull(savedToken.getRevokedAt());
    assertNull(savedToken.getReplacedBy());
    verify(refreshTokenGeneratorPort).generateRawToken();
    verify(refreshTokenGeneratorPort).hash("raw-refresh-token");
  }

  @Test
  void throwsGenericErrorAndDoesNotGenerateTokenWhenUserDoesNotExist() {
    when(userRepositoryPort.findByUsername("unknown")).thenReturn(Optional.empty());

    var exception = assertThrows(GenericErrorException.class,
      () -> refreshTokenService.generateRefreshToken("unknown"));

    assertEquals("Not found user", exception.getMessage());
    verify(refreshTokenGeneratorPort, never()).generateRawToken();
    verify(refreshTokenPort, never()).save(any(RefreshToken.class));
  }

  @Test
  void rotatesValidRefreshTokenAndReturnsNewAccessAndRefreshTokens() {
    var currentToken = createRefreshToken("old-hash", null, null, NOW.plusSeconds(3600));
    var user = createUser(List.of(
      new Role(1L, "ROLE_ADMIN", "Administrator", USER_ID),
      new Role(2L, "ROLE_USER", "Standard user", USER_ID)
    ));
    when(refreshTokenGeneratorPort.hash("old-raw-token")).thenReturn("old-hash");
    when(refreshTokenPort.findByTokenHashForUpdate("old-hash"))
      .thenReturn(Optional.of(currentToken));
    when(refreshTokenGeneratorPort.generateRawToken()).thenReturn("new-raw-token");
    when(refreshTokenGeneratorPort.hash("new-raw-token")).thenReturn("new-hash");
    when(userRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(user));
    when(tokenProviderPort.generateToken(any(AuthenticatedUser.class))).thenReturn("new-access-token");

    AuthRefreshToken result = refreshTokenService.getRefreshToken("old-raw-token");

    assertEquals(new AuthRefreshToken("new-access-token", "new-raw-token"), result);
    assertTrue(currentToken.isRevoked());
    assertEquals(NOW, currentToken.getRevokedAt());
    assertEquals("new-hash", currentToken.getReplacedBy());

    var savedTokensCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenPort, times(2)).save(savedTokensCaptor.capture());
    List<RefreshToken> savedTokens = savedTokensCaptor.getAllValues();
    assertSame(currentToken, savedTokens.get(0));
    RefreshToken nextToken = savedTokens.get(1);
    assertEquals(USER_ID, nextToken.getUserId());
    assertEquals(FAMILY_ID, nextToken.getFamilyId());
    assertEquals("new-hash", nextToken.getTokenHash());
    assertEquals(NOW.plusSeconds(7 * 24 * 60 * 60L), nextToken.getExpiresAt());
    assertFalse(nextToken.isRevoked());

    var authenticatedUserCaptor = ArgumentCaptor.forClass(AuthenticatedUser.class);
    verify(tokenProviderPort).generateToken(authenticatedUserCaptor.capture());
    assertEquals("jane.doe", authenticatedUserCaptor.getValue().userId());
    assertEquals("jane.doe", authenticatedUserCaptor.getValue().username());
    assertEquals(Set.of("ROLE_ADMIN", "ROLE_USER"), authenticatedUserCaptor.getValue().roles());
    assertEquals("Jane", authenticatedUserCaptor.getValue().firstName());
    assertEquals("Doe", authenticatedUserCaptor.getValue().lastName());
  }

  @Test
  void throwsInvalidTokenWhenRefreshTokenHashDoesNotExist() {
    when(refreshTokenGeneratorPort.hash("unknown-token")).thenReturn("unknown-hash");
    when(refreshTokenPort.findByTokenHashForUpdate("unknown-hash")).thenReturn(Optional.empty());

    var exception = assertThrows(
      UnauthorizedException.class,
      () -> refreshTokenService.getRefreshToken("unknown-token")
    );

    assertEquals("Not refresh token", exception.getMessage());
    verify(refreshTokenGeneratorPort, never()).generateRawToken();
    verify(userRepositoryPort, never()).findByUserId(any());
    verify(refreshTokenPort, never()).save(any(RefreshToken.class));
    verify(refreshTokenPort, never()).revokeFamily(any(), any());
  }

  @Test
  void revokesTokenFamilyAndRejectsPreviouslyRevokedToken() {
    var revokedToken = createRefreshToken("old-hash", NOW.minusSeconds(1), "replacement-hash",
      NOW.plusSeconds(3600));
    when(refreshTokenGeneratorPort.hash("replayed-token")).thenReturn("old-hash");
    when(refreshTokenPort.findByTokenHashForUpdate("old-hash"))
      .thenReturn(Optional.of(revokedToken));

    var exception = assertThrows(
      UnauthorizedException.class,
      () -> refreshTokenService.getRefreshToken("replayed-token")
    );

    assertEquals("Request new session", exception.getMessage());
    verify(refreshTokenPort).revokeFamily(FAMILY_ID, NOW);
    verify(refreshTokenGeneratorPort, never()).generateRawToken();
    verify(refreshTokenPort, never()).save(any(RefreshToken.class));
    verify(userRepositoryPort, never()).findByUserId(any());
  }

  @Test
  void rejectsExpiredRefreshTokenWithoutRotatingOrRevokingFamily() {
    var expiredToken = createRefreshToken("expired-hash", null, null, NOW.minusSeconds(1));
    when(refreshTokenGeneratorPort.hash("expired-token")).thenReturn("expired-hash");
    when(refreshTokenPort.findByTokenHashForUpdate("expired-hash"))
      .thenReturn(Optional.of(expiredToken));

    var exception = assertThrows(
      UnauthorizedException.class,
      () -> refreshTokenService.getRefreshToken("expired-token")
    );

    assertEquals("Token is expired, request new session", exception.getMessage());
    verify(refreshTokenPort, never()).revokeFamily(any(), any());
    verify(refreshTokenGeneratorPort, never()).generateRawToken();
    verify(refreshTokenPort, never()).save(any(RefreshToken.class));
    verify(userRepositoryPort, never()).findByUserId(any());
  }

  @Test
  void rejectsValidRefreshTokenWhenItsUserNoLongerExists() {
    var currentToken = createRefreshToken("old-hash", null, null, NOW.plusSeconds(3600));
    when(refreshTokenGeneratorPort.hash("old-raw-token")).thenReturn("old-hash");
    when(refreshTokenPort.findByTokenHashForUpdate("old-hash"))
      .thenReturn(Optional.of(currentToken));
    when(refreshTokenGeneratorPort.generateRawToken()).thenReturn("new-raw-token");
    when(refreshTokenGeneratorPort.hash("new-raw-token")).thenReturn("new-hash");
    when(userRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.empty());

    var exception = assertThrows(
      UnauthorizedException.class,
      () -> refreshTokenService.getRefreshToken("old-raw-token")
    );

    assertEquals("User no found", exception.getMessage());
    assertFalse(currentToken.isRevoked());
    verify(tokenProviderPort, never()).generateToken(any(AuthenticatedUser.class));
    verify(refreshTokenPort, never()).save(any(RefreshToken.class));
  }

  @Test
  void logoutRevokesFamilyWhenRefreshTokenExists() {
    var token = createRefreshToken("token-hash", null, null, NOW.plusSeconds(3600));
    when(refreshTokenGeneratorPort.hash("raw-token")).thenReturn("token-hash");
    when(refreshTokenPort.findByTokenHash("token-hash")).thenReturn(Optional.of(token));

    refreshTokenService.logout("raw-token");

    verify(refreshTokenGeneratorPort).hash("raw-token");
    verify(refreshTokenPort).findByTokenHash("token-hash");
    verify(refreshTokenPort).revokeFamily(FAMILY_ID, NOW);
  }

  @Test
  void logoutDoesNothingWhenRefreshTokenDoesNotExist() {
    when(refreshTokenGeneratorPort.hash("unknown-token")).thenReturn("unknown-hash");
    when(refreshTokenPort.findByTokenHash("unknown-hash")).thenReturn(Optional.empty());

    refreshTokenService.logout("unknown-token");

    verify(refreshTokenPort).findByTokenHash("unknown-hash");
    verify(refreshTokenPort, never()).revokeFamily(any(), any());
  }

  @Test
  void propagatesRepositoryFailureWhenLookingUpRefreshTokenForUpdate() {
    var repositoryException = new IllegalStateException("Refresh token repository unavailable");
    when(refreshTokenGeneratorPort.hash("raw-token")).thenReturn("token-hash");
    when(refreshTokenPort.findByTokenHashForUpdate("token-hash")).thenThrow(repositoryException);

    var exception = assertThrows(
      IllegalStateException.class,
      () -> refreshTokenService.getRefreshToken("raw-token")
    );

    assertSame(repositoryException, exception);
    verify(refreshTokenPort, never()).save(any(RefreshToken.class));
  }

  private User createUser(List<Role> roles) {
    return User.builder()
      .id(RefreshTokenServiceTest.USER_ID)
      .username("jane.doe")
      .roles(roles)
      .firstName("Jane")
      .lastName("Doe")
      .build();
  }

  private RefreshToken createRefreshToken(
    String tokenHash,
    Instant revokedAt,
    String replacedBy,
    Instant expiresAt
  ) {
    return RefreshToken.builder()
      .id(UUID.randomUUID())
      .userId(USER_ID)
      .familyId(FAMILY_ID)
      .tokenHash(tokenHash)
      .expiresAt(expiresAt)
      .revokedAt(revokedAt)
      .replacedBy(replacedBy)
      .build();
  }


}
