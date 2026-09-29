package com.example.ecommerce.catagol.infrastructure.adapter.out.external;

import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.domain.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FakeStoreClientAdapter implements ExternalCatalogPort {

  private final FakeStoreFeignClient feignClient;

  public FakeStoreClientAdapter(FakeStoreFeignClient feignClient) {
    this.feignClient = feignClient;
  }

  @Override
  @Retry(name = "fakeStore", fallbackMethod = "fallbackFetchProducts")
  @CircuitBreaker(name = "fakeStore")
  public List<Product> fetchAllProducts() {
    return this.feignClient.getAllProducts();
  }

  public List<Product> fallbackFetchProducts(Throwable t) {
    return List.of();
  }


}
