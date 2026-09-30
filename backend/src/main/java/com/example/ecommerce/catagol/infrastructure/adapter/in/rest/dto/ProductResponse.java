package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

public record ProductResponse(

  Long id,
  String title,
  BigDecimal price,
  String description,
  String category,
  RatingResponse rating,
  String note


) {
}
