package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ProductNoteService implements SaveProductNoteUseCase, FindAllProductNoteUseCase {

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

  @Override
  public List<ProductNoteResponse> findAll() {
    List<ProductNote> productNotes = this.productNoteRepositoryPort.findAll();

    if(CollectionUtils.isEmpty(productNotes)) {
      throw new EmptyProductNoteException("There are no recorded notes for the products.");
    }
    return productNotes.stream()
      .map(productNote -> new ProductNoteResponse(
        productNote.getNoteId(),
        productNote.getExtProdId(),
        productNote.getNote(),
        productNote.getCreatedBy()
      ))
      .toList();
  }



}
