package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception;

import com.example.ecommerce.catagol.domain.exception.NotFoundResourceException;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.exception.ResourceDuplicateException;
import com.example.ecommerce.catagol.domain.exception.UnauthorizedException;
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
  void returnsNotFoundWhenResourceNotFound() {
    var exception = new NotFoundResourceException("Resource not found.");

    var response = exceptionHandler.handleNotFoundResourceException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Resource not found.", response.getBody());
  }

  @Test
  void returnsInternalServerErrorWhenGenericErrorOccurs() {
    var exception = new GenericErrorException("An generic error occurred.");

    var response = exceptionHandler.handleGenericErrorException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("An generic error occurred.", response.getBody());
  }

  @Test
  void returnsDuplicateResourceWhenResourceIsDuplicate() {
    var exception = new ResourceDuplicateException("The resource is duplicate.");

    var response = exceptionHandler.handleResourceDuplicateException(exception);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("The resource is duplicate.", response.getBody());
  }

  @Test
  void returnsUnauthorizedWhenUserIsUnauthorized() {
    var exception = new UnauthorizedException("The resource is unauthorized.");

    var response = exceptionHandler.handleUnauthorizedException(exception);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("The resource is unauthorized.", response.getBody());
  }


}
