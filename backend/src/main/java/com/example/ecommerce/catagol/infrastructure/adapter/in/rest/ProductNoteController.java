package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/prodnotes")
public class ProductNoteController {

  private final SaveProductNoteUseCase saveProductNoteUseCase;

  public ProductNoteController(SaveProductNoteUseCase saveProductNoteUseCase) {
    this.saveProductNoteUseCase = saveProductNoteUseCase;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ProductNoteResponse> saveProductNote(
      @Valid @RequestBody ProductNoteRequest productNoteRequest) {
    var productNote = this.saveProductNoteUseCase.saveProductNote(productNoteRequest);
    return ResponseEntity.ok(productNote);
  }


}
