package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

public class ProductNoteService implements SaveProductNoteUseCase, FindAllProductNoteUseCase {

  private final ProductNoteRepositoryPort productNoteRepositoryPort;
  private final AuditLogRepositoryPort auditLogRepositoryPort;

  public ProductNoteService(ProductNoteRepositoryPort productNoteRepositoryPort,
                            AuditLogRepositoryPort auditLogRepositoryPort) {
    this.productNoteRepositoryPort = productNoteRepositoryPort;
    this.auditLogRepositoryPort = auditLogRepositoryPort;
  }

  @Override
  public ProductNoteResponse saveProductNote(ProductNoteRequest productNoteRequest)
      throws ProductNoteDuplicateException{
    long startTime = System.currentTimeMillis();
    String status = "SUCCESS";
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
      status = "FAILED";
      throw new ProductNoteDuplicateException("The Product Note is duplicate");
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      var auditLog = this.getAuditLog(duration, status);
      this.auditLogRepositoryPort.save(auditLog);
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

  private AuditLog getAuditLog(Long duration, String status) {
    return AuditLog.builder()
      .operation("SAVE PRODUCT NOTE")
      .durationMs(duration)
      .error(status.equals("FAILED") ? "Error fetching products" : null)
      .registerDate(LocalDateTime.now())
      .status(status)
      .createdBy("REGISTER")
      .build();
  }



}
