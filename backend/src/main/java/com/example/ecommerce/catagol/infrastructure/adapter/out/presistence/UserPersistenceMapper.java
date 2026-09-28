package com.example.ecommerce.catagol.infrastructure.adapter.out.presistence;

import com.example.ecommerce.catagol.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

  public User mapToDomain(UserJpaEntity userJpaEntity) {
    return User.builder()
      .id(userJpaEntity.getUserId())
      .username(userJpaEntity.getUsername())
      .password(userJpaEntity.getPassword())
      .roles(userJpaEntity.getRoles())
      .build();
  }


}
