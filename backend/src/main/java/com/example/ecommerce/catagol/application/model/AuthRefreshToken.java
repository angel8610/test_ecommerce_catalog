package com.example.ecommerce.catagol.application.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthRefreshToken(

  String accessToken,
  String refreshToken


) {
}
