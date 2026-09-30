package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.domain.model.Product;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  private final GetProductUseCase productUseCase;

  public ProductController(GetProductUseCase productUseCase) {
    this.productUseCase = productUseCase;
  }

  @GetMapping(path = "/all")
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ResponseEntity<List<ProductResponse>> getAll() {
    var products = productUseCase.getEnrichedCatalog();
    return ResponseEntity.ok(products);
  }


}
