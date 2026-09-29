package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prodnotes")
public class ProductNoteController {

  private final SaveProductNoteUseCase saveProductNoteUseCase;
  private final FindAllProductNoteUseCase findAllProductNoteUseCase;

  public ProductNoteController(SaveProductNoteUseCase saveProductNoteUseCase,
                               FindAllProductNoteUseCase findAllProductNoteUseCase) {
    this.saveProductNoteUseCase = saveProductNoteUseCase;
    this.findAllProductNoteUseCase = findAllProductNoteUseCase;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ProductNoteResponse> saveProductNote(
      @Valid @RequestBody ProductNoteRequest productNoteRequest) {
    var productNote = this.saveProductNoteUseCase.saveProductNote(productNoteRequest);
    return ResponseEntity.ok(productNote);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ResponseEntity<List<ProductNoteResponse>> findAll() {
    var productNotes = this.findAllProductNoteUseCase.findAll();
    return ResponseEntity.ok(productNotes);
  }


}
