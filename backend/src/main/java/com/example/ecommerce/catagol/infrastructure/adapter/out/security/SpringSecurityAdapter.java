package com.example.ecommerce.catagol.infrastructure.adapter.out.security;

import com.example.ecommerce.catagol.application.port.out.AuthenticationPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAdapter implements AuthenticationPort {

  @Override
  public Optional<String> getAuthenticatedUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() ||
      authentication instanceof AnonymousAuthenticationToken) {
      return Optional.empty();
    }

    return Optional.ofNullable(authentication.getName());
  }

  @Override
  public boolean hasRole(String role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return false;
    }

    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;

    return authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals(roleWithPrefix));
  }


}
