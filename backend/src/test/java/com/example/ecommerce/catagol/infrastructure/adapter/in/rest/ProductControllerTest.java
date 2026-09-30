package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.RatingResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock
  private GetProductUseCase productUseCase;

  private ProductController productController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    productController = new ProductController(productUseCase);
    mockMvc = standaloneSetup(productController)
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }

  @Test
  void returnsOkWithAllProductsFromUseCase() {
    var products = List.of(
      new ProductResponse(1L, "Chair", new BigDecimal("49.99"), "Comfortable chair",
        "Furniture", new RatingResponse(4.5, 12), "Durable material"),
      new ProductResponse(2L, "Table", new BigDecimal("89.50"), "Wooden table",
        "Furniture", new RatingResponse(4.2, 8), "")
    );
    when(productUseCase.getEnrichedCatalog()).thenReturn(products);

    ResponseEntity<List<ProductResponse>> response = productController.findAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(products, response.getBody());
    verify(productUseCase).getEnrichedCatalog();
  }

  @Test
  void returnsOkWithEmptyListWhenUseCaseReturnsNoProducts() {
    when(productUseCase.getEnrichedCatalog()).thenReturn(List.of());

    ResponseEntity<List<ProductResponse>> response = productController.findAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(List.of(), response.getBody());
    verify(productUseCase).getEnrichedCatalog();
  }

  @Test
  void propagatesEmptyProductApiExceptionWhenUseCaseHasNoCatalogResults() {
    var emptyCatalogException = new EmptyProductAPIException("No results were obtained from the API.");
    when(productUseCase.getEnrichedCatalog()).thenThrow(emptyCatalogException);

    var exception = assertThrows(EmptyProductAPIException.class, productController::findAll);

    assertSame(emptyCatalogException, exception);
    verify(productUseCase).getEnrichedCatalog();
  }

  @Test
  void returnsProductCatalogAsJsonWhenProductsEndpointIsCalled() throws Exception {
    when(productUseCase.getEnrichedCatalog()).thenReturn(List.of(
      new ProductResponse(1L, "Chair", new BigDecimal("49.99"), "Comfortable chair",
        "Furniture", new RatingResponse(4.5, 12), "Durable material")
    ));

    mockMvc.perform(get("/api/v1/products"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].title").value("Chair"))
      .andExpect(jsonPath("$[0].price").value(49.99))
      .andExpect(jsonPath("$[0].description").value("Comfortable chair"))
      .andExpect(jsonPath("$[0].category").value("Furniture"))
      .andExpect(jsonPath("$[0].rating.rate").value(4.5))
      .andExpect(jsonPath("$[0].rating.count").value(12))
      .andExpect(jsonPath("$[0].note").value("Durable material"));

    verify(productUseCase).getEnrichedCatalog();
  }

  @Test
  void returnsEmptyJsonArrayWhenProductsEndpointHasNoProducts() throws Exception {
    when(productUseCase.getEnrichedCatalog()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/products"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());

    verify(productUseCase).getEnrichedCatalog();
  }

  @Test
  void returnsNotFoundWhenProductsEndpointHasNoCatalogResults() throws Exception {
    when(productUseCase.getEnrichedCatalog())
      .thenThrow(new EmptyProductAPIException("No results were obtained from the API."));

    mockMvc.perform(get("/api/v1/products"))
      .andExpect(status().isNotFound())
      .andExpect(content().string("No results were obtained from the API."));

    verify(productUseCase).getEnrichedCatalog();
  }


}
