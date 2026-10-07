package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.Product;
import com.example.ecommerce.catagol.application.model.ProductCatalogItem;
import com.example.ecommerce.catagol.application.model.Rating;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ExternalCatalogPort externalCatalogPort;

  @Mock
  private ProductNoteRepositoryPort productNoteRepositoryPort;

  private ProductService productService;

  @BeforeEach
  void setUp() {
    productService = new ProductService(
      externalCatalogPort,
      productNoteRepositoryPort
    );
  }

  @Test
  void returnsEnrichedProductsAndWritesSuccessfulAuditLog() {
    var firstProduct = createProduct(1L, "Chair", "49.99");
    var secondProduct = createProduct(2L, "Table", "89.50");
    when(externalCatalogPort.fetchAllProducts()).thenReturn(List.of(firstProduct, secondProduct));
    when(productNoteRepositoryPort.findAll()).thenReturn(List.of(
      createProductNote(1L, "Comfortable"),
      createProductNote(1L, "Duplicate note"),
      createProductNote(3L, "Note for another product")
    ));

    List<ProductCatalogItem> responses = productService.getEnrichedCatalog();

    assertEquals(
      List.of(
        new ProductCatalogItem(1L, "Chair", new BigDecimal("49.99"), "Description 1",
          "Furniture", new Rating(4.5, 12), "Comfortable"),
        new ProductCatalogItem(2L, "Table", new BigDecimal("89.50"), "Description 2",
          "Furniture", new Rating(4.5, 12), "")
      ),
      responses
    );
  }

  @Test
  void throwsEmptyProductApiExceptionWhenCatalogReturnsNoProducts() {
    when(externalCatalogPort.fetchAllProducts()).thenReturn(List.of());

    var exception = assertThrows(EmptyProductAPIException.class, productService::getEnrichedCatalog);

    assertEquals("No results were obtained from the API.", exception.getMessage());
    verify(productNoteRepositoryPort, never()).findAll();
  }

  @Test
  void throwsEmptyProductApiExceptionWhenCatalogReturnsNull() {
    when(externalCatalogPort.fetchAllProducts()).thenReturn(null);

    assertThrows(EmptyProductAPIException.class, productService::getEnrichedCatalog);

    verify(productNoteRepositoryPort, never()).findAll();
  }

  @Test
  void propagatesCatalogErrors() {
    var catalogException = new IllegalStateException("Catalog unavailable");
    when(externalCatalogPort.fetchAllProducts()).thenThrow(catalogException);

    var exception = assertThrows(IllegalStateException.class, productService::getEnrichedCatalog);

    assertSame(catalogException, exception);
    verify(productNoteRepositoryPort, never()).findAll();
  }

  @Test
  void wrapsProductNoteRepositoryErrorsInGenericErrorException() {
    when(externalCatalogPort.fetchAllProducts())
      .thenReturn(List.of(createProduct(1L, "Chair", "49.99")));
    when(productNoteRepositoryPort.findAll())
      .thenThrow(new IllegalStateException("Notes repository unavailable"));

    var exception = assertThrows(GenericErrorException.class, productService::getEnrichedCatalog);

    assertEquals("An error occurred while processing the product catalog.", exception.getMessage());
    verify(productNoteRepositoryPort).findAll();
  }

  @Test
  void wrapsProductConversionErrorsInGenericErrorException() {
    var productWithoutRating = new Product(
      1L,
      "Chair",
      new BigDecimal("49.99"),
      "Description 1",
      "Furniture",
      null
    );
    when(externalCatalogPort.fetchAllProducts()).thenReturn(List.of(productWithoutRating));
    when(productNoteRepositoryPort.findAll()).thenReturn(List.of());

    var exception = assertThrows(GenericErrorException.class, productService::getEnrichedCatalog);

    assertEquals("An error occurred while processing the product catalog.", exception.getMessage());
    verify(productNoteRepositoryPort).findAll();
  }

  private Product createProduct(Long id, String title, String price) {
    return new Product(id, title, new BigDecimal(price), "Description " + id, "Furniture",
      new Rating(4.5, 12));
  }

  private ProductNote createProductNote(Long extProdId, String note) {
    return ProductNote.builder()
      .extProdId(extProdId)
      .note(note)
      .build();
  }


}
