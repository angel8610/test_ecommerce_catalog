package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductNoteRequest(

  @NotNull(message = "The external product ID field is required")
  Long extProdId,

  @NotBlank(message = "The note field is required")
  @Pattern(regexp = "^[\\p{L}\\s]+$", message = "The note field is invalid")
  @Size(max = 1000, message = "The note note must be less than 1000 characters")
  String note,

  @NotBlank(message = "The created by field is required")
  @Pattern(regexp = "^[\\p{L}\\s]+$", message = "The created by field is invalid")
  @Size(max = 255, message = "The created by field must be less than 255 characters")
  String createdBy


) {
}
