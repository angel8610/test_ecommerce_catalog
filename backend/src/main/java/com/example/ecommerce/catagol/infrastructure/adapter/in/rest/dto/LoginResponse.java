package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

public record LoginResponse(
  String accessToken,
  String tokenType,
  long expiresIn
) {}
