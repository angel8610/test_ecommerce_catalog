package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

import java.util.Set;

public record AuthenticatedUserResponse(

  String userId,
  String username,
  Set<String> roles,
  String firstName,
  String lastName


) {

  public AuthenticatedUserResponse {
    roles = Set.copyOf(roles);
  }


}
