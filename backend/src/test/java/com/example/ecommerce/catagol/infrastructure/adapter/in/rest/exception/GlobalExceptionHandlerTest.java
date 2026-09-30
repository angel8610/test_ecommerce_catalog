package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception;

import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.AuthController;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.FieldError;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ValidationErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

  @Test
  void returnsNotFoundWithMessageWhenUsernameIsUnknown() {
    var exception = new UsernameNotFoundException("Invalid credentials");

    var response = exceptionHandler.handleUsernameNotFoundException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Invalid credentials", response.getBody());
  }

  @Test
  void returnsBadRequestWithValidationDetailsWhenArgumentsAreInvalid() throws Exception {
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(
      new org.springframework.validation.FieldError("loginRequest", "username",
        "must not be blank"),
      new org.springframework.validation.FieldError("loginRequest", "password",
        "must not be blank")
    ));
    Method loginMethod = AuthController.class.getMethod("login", LoginRequest.class);
    var methodParameter = new MethodParameter(loginMethod, 0);
    var exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

    var response = exceptionHandler.handleValidation(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ValidationErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(400, body.status());
    assertEquals("Validation failed", body.message());
    assertNotNull(body.timestamp());
    assertEquals(
      List.of(
        new FieldError("username", "must not be blank"),
        new FieldError("password", "must not be blank")
      ),
      body.errors()
    );
  }

  @Test
  void returnsConflictWithMessageWhenProductNoteIsDuplicate() {
    var exception = new ProductNoteDuplicateException("The Product Note is duplicate");

    var response = exceptionHandler.handleProductNoteDuplicateException(exception);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("The Product Note is duplicate", response.getBody());
  }

  @Test
  void returnsNotFoundWithMessageWhenProductCatalogIsEmpty() {
    var exception = new EmptyProductAPIException("No results were obtained from the API.");

    var response = exceptionHandler.handleEmptyProductAPIException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("No results were obtained from the API.", response.getBody());
  }

  @Test
  void returnsNotFoundWithMessageWhenProductNotesAreEmpty() {
    var exception = new EmptyProductNoteException("There are no recorded notes for the products.");

    var response = exceptionHandler.handleEmptyProductNoteException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("There are no recorded notes for the products.", response.getBody());
  }


}
