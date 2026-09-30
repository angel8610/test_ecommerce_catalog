package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.domain.model.Role;
import com.example.ecommerce.catagol.domain.model.User;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.RoleJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.UserJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.UserPersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpingDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryAdapterTest {

  @Mock
  private SpingDataUserRepository spingDataUserRepository;

  private JpaUserRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new JpaUserRepositoryAdapter(spingDataUserRepository, new UserPersistenceMapper());
  }

  @Test
  void returnsMappedUserWithRolesWhenUsernameExists() {
    var userEntity = UserJpaEntity.builder()
      .userId(7L)
      .username("jane.doe")
      .password("encoded-password")
      .roles(List.of(
        RoleJpaEntity.builder()
          .roleId(2L)
          .name("ROLE_ADMIN")
          .description("Administrator")
          .userId(7L)
          .build(),
        RoleJpaEntity.builder()
          .roleId(3L)
          .name("ROLE_USER")
          .description("Standard user")
          .userId(7L)
          .build()
      ))
      .build();
    when(spingDataUserRepository.findByUsername("jane.doe")).thenReturn(Optional.of(userEntity));

    Optional<User> result = adapter.findByUsername("jane.doe");

    assertEquals(
      Optional.of(User.builder()
        .id(7L)
        .username("jane.doe")
        .password("encoded-password")
        .roles(List.of(
          new Role(2L, "ROLE_ADMIN", "Administrator", 7L),
          new Role(3L, "ROLE_USER", "Standard user", 7L)
        ))
        .build()),
      result
    );
    verify(spingDataUserRepository).findByUsername("jane.doe");
  }

  @Test
  void returnsEmptyWhenUsernameDoesNotExist() {
    when(spingDataUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    Optional<User> result = adapter.findByUsername("unknown");

    assertEquals(Optional.empty(), result);
    verify(spingDataUserRepository).findByUsername("unknown");
  }

  @Test
  void propagatesRepositoryErrorsWhenSearchingByUsername() {
    var repositoryException = new IllegalStateException("User database unavailable");
    when(spingDataUserRepository.findByUsername("jane.doe")).thenThrow(repositoryException);

    var exception = assertThrows(
      IllegalStateException.class,
      () -> adapter.findByUsername("jane.doe")
    );

    assertSame(repositoryException, exception);
    verify(spingDataUserRepository).findByUsername("jane.doe");
  }


}
