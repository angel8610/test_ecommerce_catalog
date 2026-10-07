package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.model.Product;
import com.example.ecommerce.catagol.application.model.ProductCatalogItem;
import com.example.ecommerce.catagol.application.model.Rating;
import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.config.audit.Auditable;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductService implements GetProductUseCase {

  private final ExternalCatalogPort externalCatalogPort;
  private final ProductNoteRepositoryPort productNoteRepositoryPort;

  public ProductService(ExternalCatalogPort externalCatalogPort,
                        ProductNoteRepositoryPort productNoteRepositoryPort) {
    this.externalCatalogPort = externalCatalogPort;
    this.productNoteRepositoryPort = productNoteRepositoryPort;
  }

  @Override
  @Auditable(operation = "GET PRODUCTS API")
  public List<ProductCatalogItem> getEnrichedCatalog() {
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
      throw new GenericErrorException("An error occurred while processing the product catalog.");
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


}
