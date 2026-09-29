package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductResponse;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class ProductService implements GetProductUseCase {

  private final ExternalCatalogPort externalCatalogPort;
  private final AuditLogRepositoryPort auditLogRepositoryPort;

  public ProductService(ExternalCatalogPort externalCatalogPort,
                        AuditLogRepositoryPort auditLogRepositoryPort) {
    this.externalCatalogPort = externalCatalogPort;
    this.auditLogRepositoryPort = auditLogRepositoryPort;
  }

  @Override
  public List<ProductResponse> getEnrichedCatalog() {
    long startTime = System.currentTimeMillis();
    String status = "SUCCESS";

    try {
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
    } catch (Exception e) {
      status = "FAILED";
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      var auditLog = AuditLog.builder()
        .endpointUrl("/product")
        .httpMethod("GET")
        .responseTimeMs(duration)
        .errorMessage(status.equals("FAILED") ? "Error fetching products" : null)
        .timestamp(LocalDateTime.from(ZonedDateTime.now()))
        .statusResponse(status)
        .build();
      this.auditLogRepositoryPort.save(auditLog);
    }
  }


}
