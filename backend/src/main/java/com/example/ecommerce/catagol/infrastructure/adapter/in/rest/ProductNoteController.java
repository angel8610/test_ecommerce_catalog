package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.ProductNoteCreateCommand;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    var productNoteCreateCommand = new ProductNoteCreateCommand(
      productNoteRequest.extProdId(),
      productNoteRequest.note()
    );
    var productNote = this.saveProductNoteUseCase.saveProductNote(productNoteCreateCommand);

    return ResponseEntity.status(HttpStatus.CREATED).body(new ProductNoteResponse(
      productNote.getNoteId(),
      productNote.getExtProdId(),
      productNote.getNote(),
      productNote.getCreatedBy()
    ));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ResponseEntity<List<ProductNoteResponse>> findAll() {
    var productNotes = this.findAllProductNoteUseCase.findAll();
    var productNoteResponses = productNotes.stream()
      .map(productNote -> new ProductNoteResponse(
        productNote.getNoteId(),
        productNote.getExtProdId(),
        productNote.getNote(),
        productNote.getCreatedBy()
      ))
      .toList();
    return ResponseEntity.ok(productNoteResponses);
  }


}
