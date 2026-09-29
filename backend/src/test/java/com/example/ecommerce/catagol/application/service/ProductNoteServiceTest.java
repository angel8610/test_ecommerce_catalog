package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductNoteServiceTest {

  @Mock
  private ProductNoteRepositoryPort productNoteRepositoryPort;

  private ProductNoteService productNoteService;

  @BeforeEach
  void setUp() {
    productNoteService = new ProductNoteService(productNoteRepositoryPort);
  }

  @Test
  void savesProductNoteAndReturnsSavedNoteDetails() {
    var request = new ProductNoteRequest(42L, "Durable material", "John Dave");
    var savedProductNote = ProductNote.builder()
      .noteId(1L)
      .extProdId(42L)
      .note("Durable material")
      .createdBy("John Dave")
      .build();
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenReturn(savedProductNote);

    ProductNoteResponse response = productNoteService.saveProductNote(request);

    assertEquals(new ProductNoteResponse(1L, 42L,
      "Durable material", "John Dave"), response);

    var productNoteCaptor = ArgumentCaptor.forClass(ProductNote.class);
    verify(productNoteRepositoryPort).save(productNoteCaptor.capture());
    assertEquals(42L, productNoteCaptor.getValue().getExtProdId());
    assertEquals("Durable material", productNoteCaptor.getValue().getNote());
    assertEquals("John Dave", productNoteCaptor.getValue().getCreatedBy());
  }

  @Test
  void throwsDuplicateExceptionWhenRepositoryRejectsDuplicateProductNote() {
    var request = new ProductNoteRequest(42L, "Durable material", "John Dave");
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenThrow(new DataIntegrityViolationException("Duplicate note"));

    var exception = assertThrows(
      ProductNoteDuplicateException.class,
      () -> productNoteService.saveProductNote(request)
    );

    assertEquals("The Product Note is duplicate", exception.getMessage());
    verify(productNoteRepositoryPort).save(any(ProductNote.class));
  }

  @Test
  void propagatesUnexpectedRepositoryErrorsWhenSavingProductNote() {
    var request = new ProductNoteRequest(42L, "Durable material", "John Dave");
    var repositoryException = new IllegalStateException("Repository unavailable");
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenThrow(repositoryException);

    var exception = assertThrows(
      IllegalStateException.class,
      () -> productNoteService.saveProductNote(request)
    );

    assertSame(repositoryException, exception);
  }

  @Test
  void returnsAllProductNotesAsResponsesInRepositoryOrder() {
    var firstProductNote = createProductNote(1L, 42L,
      "Durable material", "John Dave");
    var secondProductNote = createProductNote(2L, 43L,
      "Easy to clean", "Jordan Lee");
    when(productNoteRepositoryPort.findAll()).thenReturn(List.of(firstProductNote, secondProductNote));

    List<ProductNoteResponse> responses = productNoteService.findAll();

    assertEquals(
      List.of(
        new ProductNoteResponse(1L, 42L, "Durable material", "John Dave"),
        new ProductNoteResponse(2L, 43L, "Easy to clean", "Jordan Lee")
      ),
      responses
    );
    verify(productNoteRepositoryPort).findAll();
  }

  @Test
  void throwsEmptyProductNoteExceptionWhenRepositoryReturnsNoNotes() {
    when(productNoteRepositoryPort.findAll()).thenReturn(List.of());

    var exception = assertThrows(EmptyProductNoteException.class, productNoteService::findAll);

    assertEquals("There are no recorded notes for the products.", exception.getMessage());
    verify(productNoteRepositoryPort).findAll();
  }

  @Test
  void throwsEmptyProductNoteExceptionWhenRepositoryReturnsNull() {
    when(productNoteRepositoryPort.findAll()).thenReturn(null);

    assertThrows(EmptyProductNoteException.class, productNoteService::findAll);
    verify(productNoteRepositoryPort).findAll();
  }

  private ProductNote createProductNote(Long noteId, Long extProdId, String note, String createdBy) {
    return ProductNote.builder()
      .noteId(noteId)
      .extProdId(extProdId)
      .note(note)
      .createdBy(createdBy)
      .build();
  }


}
