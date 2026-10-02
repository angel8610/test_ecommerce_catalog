package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.Product;
import com.example.ecommerce.catagol.application.model.ProductCatalogItem;
import com.example.ecommerce.catagol.application.model.Rating;
import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
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
  public List<ProductCatalogItem> getEnrichedCatalog() {
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
    } catch(Exception e) {
      status = "FAILED";
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      var auditLog = this.getAuditLog(duration, status);
      this.auditLogRepositoryPort.save(auditLog);
    }
  }

  private List<ProductCatalogItem> convertToListProductResponse(List<Product> products,
                                                             Map<Long, String> notesMap) {
    return products.stream()
      .map(product -> {
        String note = notesMap.getOrDefault(product.id(), "");
        var rating = product.rating();
        return new ProductCatalogItem(
          product.id(),
          product.title(),
          product.price(),
          product.description(),
          product.category(),
          new Rating(rating.rate(), rating.count()),
          note
        );
      })
      .toList();
  }

  private AuditLog getAuditLog(Long duration, String status) {
    return AuditLog.builder()
      .operation("GET PRODUCTS API")
      .durationMs(duration)
      .error(status.equals("FAILED") ? "Error fetching products" : null)
      .registerDate(LocalDateTime.now())
      .status(status)
      .createdBy("API")
      .build();
  }


}
