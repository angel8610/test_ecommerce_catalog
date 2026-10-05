package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.Role;
import com.example.ecommerce.catagol.domain.model.User;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

  public User mapToDomain(UserJpaEntity userJpaEntity) {
    var roles = userJpaEntity.getRoles().stream()
      .map(roleJpaEntity -> new Role(
        roleJpaEntity.getRoleId(),
        roleJpaEntity.getName(),
        roleJpaEntity.getDescription(),
        roleJpaEntity.getUserId())
      )
      .toList();

    return User.builder()
      .id(userJpaEntity.getUserId())
      .username(userJpaEntity.getUsername())
      .password(userJpaEntity.getPassword())
      .roles(roles)
      .firstName(userJpaEntity.getFirstName())
      .lastName(userJpaEntity.getLastName())
      .build();
  }


}
