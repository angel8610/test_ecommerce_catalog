package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.AuthRefreshToken;
import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.port.in.GenerateRefreshTokenUseCase;
import com.example.ecommerce.catagol.application.port.in.LogoutUseCase;
import com.example.ecommerce.catagol.application.port.in.GetRefreshTokenUseCase;
import com.example.ecommerce.catagol.application.port.out.*;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.exception.UnauthorizedException;
import com.example.ecommerce.catagol.domain.model.RefreshToken;
import com.example.ecommerce.catagol.domain.model.Role;
import com.example.ecommerce.catagol.infrastructure.config.SecurityRefreshConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RefreshTokenService implements GenerateRefreshTokenUseCase, GetRefreshTokenUseCase,
  LogoutUseCase {

  private final Clock clock;
  private final RefreshTokenPort refreshTokenPort;
  private final RefreshTokenGeneratorPort refreshTokenGeneratorPort;
  private final UserRepositoryPort userRepositoryPort;
  private final TokenProviderPort tokenProviderPort;
  private final SecurityRefreshConfig securityRefreshConfig;

  public RefreshTokenService(Clock clock, RefreshTokenPort refreshTokenPort,
                             RefreshTokenGeneratorPort refreshTokenGeneratorPort,
                             UserRepositoryPort userRepositoryPort,
                             TokenProviderPort tokenProviderPort,
                             SecurityRefreshConfig securityRefreshConfig) {
    this.clock = clock;
    this.refreshTokenPort = refreshTokenPort;
    this.refreshTokenGeneratorPort = refreshTokenGeneratorPort;
    this.userRepositoryPort = userRepositoryPort;
    this.tokenProviderPort = tokenProviderPort;
    this.securityRefreshConfig = securityRefreshConfig;
  }

  @Override
  @Transactional
  public AuthRefreshToken generateRefreshToken(String userName) {
    var user = this.userRepositoryPort.findByUsername(userName)
      .orElseThrow(() -> new GenericErrorException("Not found user"));
    String raw = this.refreshTokenGeneratorPort.generateRawToken();

    var refreshToken = this.buildRefreshToken(user.getId(), UUID.randomUUID(), raw);
    refreshTokenPort.save(refreshToken);

    return new AuthRefreshToken(null, raw);
  }

  @Override
  @Transactional
  public AuthRefreshToken getRefreshToken(String rawToken) {
    Instant now = clock.instant();
    String hash = this.refreshTokenGeneratorPort.hash(rawToken);

    RefreshToken current = refreshTokenPort.findByTokenHashForUpdate(hash)
      .orElseThrow(() -> new UnauthorizedException("Not refresh token"));

    if(current.isRevoked()) {
      refreshTokenPort.revokeFamily(current.getFamilyId(), now);
      throw new UnauthorizedException("Request new session");
    }

    if(current.isExpired(now)) {
      throw new UnauthorizedException("Token is expired, request new session");
    }

    String newRaw = this.refreshTokenGeneratorPort.generateRawToken();
    var next = this.buildRefreshToken(current.getUserId(), current.getFamilyId(), newRaw);

    var user = this.userRepositoryPort.findByUserId(current.getUserId()).
      orElseThrow(() -> new UnauthorizedException("User no found"));
    Set<String> roles = user.getRoles().stream()
      .map(Role::getName)
      .collect(Collectors.toUnmodifiableSet());

    AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getUsername(), user.getUsername(), roles,
      user.getFirstName(), user.getLastName());
    var token = tokenProviderPort.generateToken(authenticatedUser);

    current.revoke(now, next.getTokenHash());
    refreshTokenPort.save(current);
    refreshTokenPort.save(next);

    return new AuthRefreshToken(token, newRaw);
  }

  @Override
  @Transactional
  public void logout(String rawToken) {
    refreshTokenPort.findByTokenHash(this.refreshTokenGeneratorPort.hash(rawToken))
      .ifPresent(t -> refreshTokenPort.revokeFamily(t.getFamilyId(), clock.instant()));
  }

  private RefreshToken buildRefreshToken(Long userId, UUID familyId, String raw) {
    String hash = this.refreshTokenGeneratorPort.hash(raw);
    return RefreshToken.builder()
      .id(UUID.randomUUID())
      .userId(userId)
      .familyId(familyId)
      .tokenHash(hash)
      .expiresAt(clock.instant().plus(Duration.ofDays(securityRefreshConfig.getExpirationDays())))
      .build();
  }


}
