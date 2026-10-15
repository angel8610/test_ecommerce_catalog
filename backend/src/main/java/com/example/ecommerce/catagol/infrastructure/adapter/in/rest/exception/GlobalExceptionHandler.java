package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception;

import com.example.ecommerce.catagol.domain.exception.*;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.FieldError;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
        LocalDateTime.now(ZoneId.systemDefault()),
        HttpStatus.BAD_REQUEST.value(),
        "Validation failed",
        errors
      );

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ResourceDuplicateException.class)
  public ResponseEntity<String> handleResourceDuplicateException(ResourceDuplicateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }

  @ExceptionHandler(NotFoundResourceException.class)
  public ResponseEntity<String> handleNotFoundResourceException(NotFoundResourceException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(GenericErrorException.class)
  public ResponseEntity<String> handleGenericErrorException(GenericErrorException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
  }


}
