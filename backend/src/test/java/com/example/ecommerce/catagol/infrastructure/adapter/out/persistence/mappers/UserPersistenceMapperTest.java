package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.Role;
import com.example.ecommerce.catagol.domain.model.User;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.RoleJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.UserJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPersistenceMapperTest {

  private final UserPersistenceMapper mapper = new UserPersistenceMapper();

  @Test
  void mapsUserAndAllRolesFromJpaEntityToDomain() {
    var userEntity = UserJpaEntity.builder()
      .userId(7L)
      .username("jane.doe")
      .password("encoded-password")
      .roles(List.of(
        createRoleEntity(2L, "ROLE_ADMIN", "Administrator"),
        createRoleEntity(3L, "ROLE_USER", "Standard user")
      ))
      .build();

    User user = mapper.mapToDomain(userEntity);

    assertEquals(7L, user.getId());
    assertEquals("jane.doe", user.getUsername());
    assertEquals("encoded-password", user.getPassword());
    assertEquals(
      List.of(
        new Role(2L, "ROLE_ADMIN", "Administrator", 7L),
        new Role(3L, "ROLE_USER", "Standard user", 7L)
      ),
      user.getRoles()
    );
  }

  @Test
  void mapsUserWithNoRolesToAnEmptyRoleList() {
    var userEntity = UserJpaEntity.builder()
      .userId(7L)
      .username("jane.doe")
      .password("encoded-password")
      .roles(List.of())
      .build();

    User user = mapper.mapToDomain(userEntity);

    assertEquals(7L, user.getId());
    assertEquals("jane.doe", user.getUsername());
    assertEquals("encoded-password", user.getPassword());
    assertEquals(List.of(), user.getRoles());
  }

  @Test
  void throwsNullPointerExceptionWhenJpaEntityIsNull() {
    assertThrows(NullPointerException.class, () -> mapper.mapToDomain(null));
  }

  private RoleJpaEntity createRoleEntity(Long roleId, String name, String description) {
    return RoleJpaEntity.builder()
      .roleId(roleId)
      .name(name)
      .description(description)
      .userId(7L)
      .build();
  }


}
