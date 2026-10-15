package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.RefreshToken;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.RefreshTokenJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.utils.DataUtil;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenPersistenceMapper {

  public RefreshToken mapToDomain(RefreshTokenJpaEntity refreshTokenJpaEntity) {
    if(refreshTokenJpaEntity == null) {
      return null;
    }

    return RefreshToken.builder()
      .id(refreshTokenJpaEntity.getId())
      .tokenHash(DataUtil.getTextSure(refreshTokenJpaEntity.getTokenHash()))
      .familyId(refreshTokenJpaEntity.getFamilyId())
      .userId(refreshTokenJpaEntity.getUserId())
      .createdAt(DataUtil.getInstantFromLocalDateTime(refreshTokenJpaEntity.getCreatedAt()))
      .expiresAt(DataUtil.getInstantFromLocalDateTime(refreshTokenJpaEntity.getExpiresAt()))
      .revokedAt(DataUtil.getInstantFromLocalDateTime(refreshTokenJpaEntity.getRevokedAt()))
      .replacedBy(refreshTokenJpaEntity.getReplacedBy())
      .build();
  }

  public RefreshTokenJpaEntity mapToEntity(RefreshToken refreshToken) {
    if(refreshToken == null) {
      return null;
    }

    return RefreshTokenJpaEntity.builder()
      .id(refreshToken.getId())
      .familyId(refreshToken.getFamilyId())
      .expiresAt(DataUtil.getLocalDateTimeFromInstant(refreshToken.getExpiresAt()))
      .userId(refreshToken.getUserId())
      .tokenHash(refreshToken.getTokenHash())
      .replacedBy(DataUtil.getTextSure(refreshToken.getReplacedBy()))
      .build();
  }


}
