package com.example.ecommerce.catagol.infrastructure.adapter.out.external;

import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FakeStoreClientAdapter implements ExternalCatalogPort {

  private final FakeStoreFeignClient feignClient;

  public FakeStoreClientAdapter(FakeStoreFeignClient feignClient) {
    this.feignClient = feignClient;
  }

  @Override
  public List<Product> fetchAllProducts() {
    return this.feignClient.getAllProducts();
  }


}
