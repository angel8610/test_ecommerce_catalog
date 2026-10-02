package com.example.ecommerce.catagol.application.model;

import java.math.BigDecimal;

public record ProductCatalogItem(

  Long id,
  String title,
  BigDecimal price,
  String description,
  String category,
  Rating rating,
  String note


) {
}
