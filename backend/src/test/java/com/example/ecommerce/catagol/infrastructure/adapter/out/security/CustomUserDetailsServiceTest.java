package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import com.example.ecommerce.catagol.application.port.out.UserRepositoryPort;
import com.example.ecommerce.catagol.domain.model.Role;
import com.example.ecommerce.catagol.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepositoryPort userRepositoryPort;

  private CustomUserDetailsService userDetailsService;

  @BeforeEach
  void setUp() {
    userDetailsService = new CustomUserDetailsService(userRepositoryPort);
  }

  @Test
  void returnsUserDetailsWithCredentialsAndAuthoritiesWhenUserExists() {
    var user = User.builder()
      .id(7L)
      .username("jane.doe")
      .password("encoded-password")
      .roles(List.of(
        new Role(2L, "ROLE_ADMIN", "Administrator", 7L),
        new Role(3L, "ROLE_USER", "Standard user", 7L)
      ))
      .build();
    when(userRepositoryPort.findByUsername("jane.doe")).thenReturn(Optional.of(user));

    UserDetails userDetails = userDetailsService.loadUserByUsername("jane.doe");

    assertEquals("jane.doe", userDetails.getUsername());
    assertEquals("encoded-password", userDetails.getPassword());
    assertEquals(
      List.of("ROLE_ADMIN", "ROLE_USER"),
      userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList()
    );
    verify(userRepositoryPort).findByUsername("jane.doe");
  }

  @Test
  void returnsUserDetailsWithoutAuthoritiesWhenUserHasNoRoles() {
    var user = User.builder()
      .username("jane.doe")
      .password("encoded-password")
      .roles(List.of())
      .build();
    when(userRepositoryPort.findByUsername("jane.doe")).thenReturn(Optional.of(user));

    UserDetails userDetails = userDetailsService.loadUserByUsername("jane.doe");

    assertEquals("jane.doe", userDetails.getUsername());
    assertEquals("encoded-password", userDetails.getPassword());
    assertTrue(userDetails.getAuthorities().isEmpty());
    verify(userRepositoryPort).findByUsername("jane.doe");
  }

  @Test
  void throwsUsernameNotFoundExceptionWhenUserDoesNotExist() {
    when(userRepositoryPort.findByUsername("unknown")).thenReturn(Optional.empty());

    var exception = assertThrows(UsernameNotFoundException.class,
      () -> userDetailsService.loadUserByUsername("unknown"));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(userRepositoryPort).findByUsername("unknown");
  }

  @Test
  void propagatesRepositoryErrorsWhenLookingUpUser() {
    var repositoryException = new IllegalStateException("User repository unavailable");
    when(userRepositoryPort.findByUsername("jane.doe")).thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class,
      () -> userDetailsService.loadUserByUsername("jane.doe"));

    assertSame(repositoryException, exception);
    verify(userRepositoryPort).findByUsername("jane.doe");
  }


}
