package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductResponse;

import java.util.List;

public class ProductService implements GetProductUseCase {

  private final ExternalCatalogPort externalCatalogPort;

  public ProductService(ExternalCatalogPort externalCatalogPort) {
    this.externalCatalogPort = externalCatalogPort;
  }

  @Override
  public List<ProductResponse> getEnrichedCatalog() {
    // TODO Logic to save
    var products = this.externalCatalogPort.fetchAllProducts();
    return products.stream()
      .map(product -> new ProductResponse(product.id(),
        product.title(),
        product.price(),
        product.description(),
        product.category(),
        product.rating())
      )
      .toList();
  }


}
