package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.ProductNoteCreateCommand;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.out.AuthenticationPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.config.audit.Auditable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ProductNoteService implements SaveProductNoteUseCase, FindAllProductNoteUseCase {

  private final ProductNoteRepositoryPort productNoteRepositoryPort;
  private final AuthenticationPort authenticationPort;

  public ProductNoteService(ProductNoteRepositoryPort productNoteRepositoryPort,
                            AuthenticationPort authenticationPort) {
    this.productNoteRepositoryPort = productNoteRepositoryPort;
    this.authenticationPort = authenticationPort;
  }

  @Override
  @Auditable(operation = "SAVE PRODUCT NOTE")
  public ProductNote saveProductNote(ProductNoteCreateCommand productNoteCreateCommand)
      throws GenericErrorException, ProductNoteDuplicateException {
    var createdBy = this.authenticationPort.getAuthenticatedUsername().orElse(null);
    if(createdBy == null) {
      throw new GenericErrorException("Not authenticated user");
    }

    var productNote = ProductNote.builder()
      .extProdId(productNoteCreateCommand.extProdId())
      .createdBy(createdBy)
      .note(productNoteCreateCommand.note())
      .build();

    /*
      Regarding timing, only duplicate notes are being considered;
      concurrency and the handling of other errors are not being taken into account.
     */
    try {
      return this.productNoteRepositoryPort.save(productNote);
    } catch (DataIntegrityViolationException ex) {
      throw new ProductNoteDuplicateException("The Product Note is duplicate");
    }
  }

  @Override
  public List<ProductNote> findAll() {
    List<ProductNote> productNotes = this.productNoteRepositoryPort.findAll();

    if(CollectionUtils.isEmpty(productNotes)) {
      throw new EmptyProductNoteException("There are no recorded notes for the products.");
    }
    return productNotes;
  }


}
