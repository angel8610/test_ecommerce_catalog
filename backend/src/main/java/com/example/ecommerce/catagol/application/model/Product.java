package com.example.ecommerce.catagol.application.model;

import java.math.BigDecimal;

public record Product(

  Long id,
  String title,
  BigDecimal price,
  String description,
  String category,
  Rating rating


) {
}
