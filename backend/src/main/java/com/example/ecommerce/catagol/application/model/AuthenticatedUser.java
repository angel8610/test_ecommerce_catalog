package com.example.ecommerce.catagol.application.model;

import java.util.Set;

public record AuthenticatedUser(
  String userId,
  String username,
  Set<String> roles
) {

  public AuthenticatedUser {
    roles = Set.copyOf(roles);
  }


}
