package com.example.ecommerce.catagol.infrastructure.adapter.out.external;

import com.example.ecommerce.catagol.application.model.Product;
import com.example.ecommerce.catagol.application.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FakeStoreClientAdapterTest {

  @Mock
  private FakeStoreFeignClient feignClient;

  private FakeStoreClientAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new FakeStoreClientAdapter(feignClient);
  }

  @Test
  void returnsProductsProvidedByFeignClient() {
    List<Product> products = List.of(
      new Product(1L, "Chair", new BigDecimal("49.99"), "Comfortable chair",
        "Furniture", new Rating(4.5, 12)),
      new Product(2L, "Table", new BigDecimal("89.50"), "Wooden table",
        "Furniture", new Rating(4.2, 8))
    );
    when(feignClient.getAllProducts()).thenReturn(products);

    List<Product> result = adapter.fetchAllProducts();

    assertSame(products, result);
    verify(feignClient).getAllProducts();
  }

  @Test
  void returnsEmptyListProvidedByFeignClient() {
    List<Product> products = List.of();
    when(feignClient.getAllProducts()).thenReturn(products);

    List<Product> result = adapter.fetchAllProducts();

    assertTrue(result.isEmpty());
    assertSame(products, result);
    verify(feignClient).getAllProducts();
  }

  @Test
  void propagatesFeignClientErrorsWhenCalledWithoutResilienceProxy() {
    var clientException = new IllegalStateException("Fake Store unavailable");
    when(feignClient.getAllProducts()).thenThrow(clientException);

    var exception = assertThrows(IllegalStateException.class, adapter::fetchAllProducts);

    assertSame(clientException, exception);
    verify(feignClient).getAllProducts();
  }

  @Test
  void returnsEmptyListFromFallbackWhenFeignClientFails() {
    var result = adapter.fallbackFetchProducts(new IllegalStateException("Fake Store unavailable"));

    assertEquals(List.of(), result);
  }


}
