package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.domain.model.Product;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductResponse;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductService implements GetProductUseCase {

  private final ExternalCatalogPort externalCatalogPort;
  private final ProductNoteRepositoryPort productNoteRepositoryPort;
  private final AuditLogRepositoryPort auditLogRepositoryPort;

  public ProductService(ExternalCatalogPort externalCatalogPort,
                        ProductNoteRepositoryPort productNoteRepositoryPort,
                        AuditLogRepositoryPort auditLogRepositoryPort) {
    this.externalCatalogPort = externalCatalogPort;
    this.auditLogRepositoryPort = auditLogRepositoryPort;
    this.productNoteRepositoryPort = productNoteRepositoryPort;
  }

  @Override
  public List<ProductResponse> getEnrichedCatalog() {
    long startTime = System.currentTimeMillis();
    String status = "SUCCESS";

    var products = this.externalCatalogPort.fetchAllProducts();
    if(CollectionUtils.isEmpty(products)) {
      throw new EmptyProductAPIException("No results were obtained from the API.");
    }

    try {
      Map<Long, String> notesMap = this.productNoteRepositoryPort.findAll().stream()
        .collect(Collectors.toMap(
          ProductNote::getExtProdId,
          ProductNote::getNote,
          (existing, replacement) -> existing
        ));

      return this.convertToListProductResponse(products, notesMap);
    } catch (Exception e) {
      status = "FAILED";
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      var auditLog = this.getAuditLog(duration, status);
      this.auditLogRepositoryPort.save(auditLog);
    }
  }

  private List<ProductResponse> convertToListProductResponse(List<Product> products,
                                                             Map<Long, String> notesMap) {
    return products.stream()
      .map(product -> {
        String note = notesMap.getOrDefault(product.id(), "");
        return new ProductResponse(
          product.id(),
          product.title(),
          product.price(),
          product.description(),
          product.category(),
          product.rating(),
          note
        );
      })
      .toList();
  }

  private AuditLog getAuditLog(Long duration, String status) {
    return AuditLog.builder()
      .endpointUrl("/product")
      .httpMethod("GET")
      .responseTimeMs(duration)
      .errorMessage(status.equals("FAILED") ? "Error fetching products" : null)
      .timestamp(LocalDateTime.from(ZonedDateTime.now()))
      .statusResponse(status)
      .build();
  }


}
