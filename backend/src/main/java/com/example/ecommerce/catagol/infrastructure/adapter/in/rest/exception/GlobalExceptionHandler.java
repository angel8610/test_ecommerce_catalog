package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception;

import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.FieldError;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidation(
    MethodArgumentNotValidException ex) {

    List<FieldError> errors = ex.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(error -> new FieldError(
        error.getField(),
        error.getDefaultMessage()))
      .toList();

    ValidationErrorResponse response =
      new ValidationErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Validation failed",
        errors
      );

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ProductNoteDuplicateException.class)
  public ResponseEntity<String> handleProductNoteDuplicateException(ProductNoteDuplicateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }

  @ExceptionHandler(EmptyProductAPIException.class)
  public ResponseEntity<String> handleEmptyProductAPIException(EmptyProductAPIException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }


}
