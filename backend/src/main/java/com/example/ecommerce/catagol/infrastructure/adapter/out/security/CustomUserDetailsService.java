package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import com.example.ecommerce.catagol.application.port.out.UserRepositoryPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepositoryPort userRepositoryPort;

  public CustomUserDetailsService(UserRepositoryPort userRepositoryPort) {
    this.userRepositoryPort = userRepositoryPort;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    var user = userRepositoryPort.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

    var authorities = user.getRoles().stream()
      .map(role -> new SimpleGrantedAuthority(role.getName()))
      .toList();
    return User
      .withUsername(user.getUsername())
      .password(user.getPassword())
      .authorities(authorities)
      .build();
  }


}
