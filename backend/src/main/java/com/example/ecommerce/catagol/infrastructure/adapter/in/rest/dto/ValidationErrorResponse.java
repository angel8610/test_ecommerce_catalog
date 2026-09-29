package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(

  LocalDateTime timestamp,
  int status,
  String message,
  List<FieldError> errors

) {
}
