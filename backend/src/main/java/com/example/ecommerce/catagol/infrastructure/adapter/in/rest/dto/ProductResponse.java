package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

import com.example.ecommerce.catagol.domain.model.Rating;

import java.math.BigDecimal;

public record ProductResponse(

  Long id,
  String title,
  BigDecimal price,
  String description,
  String category,
  Rating rating


) {
}
