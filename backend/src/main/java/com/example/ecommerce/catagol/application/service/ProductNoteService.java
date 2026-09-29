package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import org.springframework.dao.DataIntegrityViolationException;

public class ProductNoteService implements SaveProductNoteUseCase {

  private final ProductNoteRepositoryPort productNoteRepositoryPort;

  public ProductNoteService(ProductNoteRepositoryPort productNoteRepositoryPort) {
    this.productNoteRepositoryPort = productNoteRepositoryPort;
  }

  @Override
  public ProductNoteResponse saveProductNote(ProductNoteRequest productNoteRequest)
      throws ProductNoteDuplicateException{
    var productNote = ProductNote.builder()
      .note(productNoteRequest.note())
      .extProdId(productNoteRequest.extProdId())
      .createdBy(productNoteRequest.createdBy())
      .build();

    /*
      Regarding timing, only duplicate notes are being considered;
      concurrency and the handling of other errors are not being taken into account.
     */
    try {
      var productNoteSave = this.productNoteRepositoryPort.save(productNote);

      return new ProductNoteResponse(
        productNoteSave.getNoteId(),
        productNoteSave.getExtProdId(),
        productNoteSave.getNote(),
        productNoteSave.getCreatedBy()
      );
    } catch (DataIntegrityViolationException ex) {
      throw new ProductNoteDuplicateException("The Product Note is duplicate");
    }
  }


}
