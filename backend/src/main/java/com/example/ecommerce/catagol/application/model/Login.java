package com.example.ecommerce.catagol.application.model;

public record Login(
  String accessToken,
  String tokenType,
  long expiresIn
) {}