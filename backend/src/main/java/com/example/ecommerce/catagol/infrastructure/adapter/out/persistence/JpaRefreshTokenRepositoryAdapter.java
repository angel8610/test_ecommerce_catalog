package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.application.port.out.RefreshTokenPort;
import com.example.ecommerce.catagol.domain.model.RefreshToken;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.RefreshTokenPersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpringRefreshTokenRepository;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.utils.DataUtil;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenPort {

  private final SpringRefreshTokenRepository springRefreshTokenRepository;
  private final RefreshTokenPersistenceMapper refreshTokenPersistenceMapper;

  public JpaRefreshTokenRepositoryAdapter(SpringRefreshTokenRepository springRefreshTokenRepository,
                                          RefreshTokenPersistenceMapper refreshTokenPersistenceMapper) {
    this.springRefreshTokenRepository = springRefreshTokenRepository;
    this.refreshTokenPersistenceMapper = refreshTokenPersistenceMapper;
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return this.springRefreshTokenRepository.findByTokenHash(tokenHash)
      .map(this.refreshTokenPersistenceMapper::mapToDomain);
  }

  @Override
  public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
    return this.springRefreshTokenRepository.findByTokenHashForUpdate(tokenHash)
      .map(this.refreshTokenPersistenceMapper::mapToDomain);
  }

  @Override
  public RefreshToken save(RefreshToken token) {
    var refreshTokenEntity = this.springRefreshTokenRepository.save(
      this.refreshTokenPersistenceMapper.mapToEntity(token));
    return this.refreshTokenPersistenceMapper.mapToDomain(refreshTokenEntity);
  }

  @Override
  public void revokeFamily(UUID familyId, Instant now) {
    this.springRefreshTokenRepository.revokeFamily(familyId, DataUtil.getLocalDateTimeFromInstant(now));
  }


}
