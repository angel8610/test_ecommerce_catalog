package com.example.ecommerce.catagol.application.service;

import com.example.ecommerce.catagol.application.port.in.ProductNoteCreateCommand;
import com.example.ecommerce.catagol.application.port.out.AuthenticationPort;
import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.GenericErrorException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductNoteServiceTest {

  private static final String NOTE = "Durable material";
  private static final String CREATED_BY = "John Dave";

  @Mock
  private ProductNoteRepositoryPort productNoteRepositoryPort;

  @Mock
  private AuthenticationPort authenticationPort;

  private ProductNoteService productNoteService;

  @BeforeEach
  void setUp() {
    productNoteService = new ProductNoteService(productNoteRepositoryPort, authenticationPort);
  }

  @Test
  void savesProductNoteAndReturnsSavedNoteDetails() {
    var productNoteCreateCommand = new ProductNoteCreateCommand(42L, "Durable material");
    var savedProductNote = ProductNote.builder()
      .noteId(1L)
      .extProdId(42L)
      .note(NOTE)
      .createdBy(CREATED_BY)
      .build();

    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.of(CREATED_BY));
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenReturn(savedProductNote);

    ProductNote productNote = productNoteService.saveProductNote(productNoteCreateCommand);

    assertEquals(savedProductNote, productNote);

    var productNoteCaptor = ArgumentCaptor.forClass(ProductNote.class);
    verify(productNoteRepositoryPort).save(productNoteCaptor.capture());
    assertEquals(42L, productNoteCaptor.getValue().getExtProdId());
    assertEquals(NOTE, productNoteCaptor.getValue().getNote());
    assertEquals(CREATED_BY, productNoteCaptor.getValue().getCreatedBy());
  }

  @Test
  void throwsGenericErrorExceptionWhenNotUsername() {
    var productNoteCreateCommand = new ProductNoteCreateCommand(42L, NOTE);

    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.empty());

    var exception = assertThrows(GenericErrorException.class,
      () -> productNoteService.saveProductNote(productNoteCreateCommand));

    assertEquals("Not authenticated user", exception.getMessage());
    verify(productNoteRepositoryPort, never()).save(any(ProductNote.class));
  }

  @Test
  void throwsDuplicateExceptionWhenRepositoryRejectsDuplicateProductNote() {
    var productNoteCreateCommand = new ProductNoteCreateCommand(42L, NOTE);

    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.of(CREATED_BY));
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenThrow(new DataIntegrityViolationException("Duplicate note"));

    var exception = assertThrows(ProductNoteDuplicateException.class,
      () -> productNoteService.saveProductNote(productNoteCreateCommand)
    );

    assertEquals("The Product Note is duplicate", exception.getMessage());
    verify(productNoteRepositoryPort).save(any(ProductNote.class));
  }

  @Test
  void propagatesUnexpectedRepositoryErrorsWhenSavingProductNote() {
    var productNoteCreateCommand = new ProductNoteCreateCommand(42L, NOTE);
    var repositoryException = new IllegalStateException("Repository unavailable");

    when(authenticationPort.getAuthenticatedUsername()).thenReturn(Optional.of(CREATED_BY));
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class,
      () -> productNoteService.saveProductNote(productNoteCreateCommand)
    );

    assertSame(repositoryException, exception);
  }

  @Test
  void returnsAllProductNotesAsResponsesInRepositoryOrder() {
    var firstProductNote = createProductNote(1L, 42L, NOTE, CREATED_BY);
    var secondProductNote = createProductNote(2L, 43L,
      "Easy to clean", "Jordan Lee");
    when(productNoteRepositoryPort.findAll()).thenReturn(List.of(firstProductNote, secondProductNote));

    List<ProductNote> responses = productNoteService.findAll();

    assertEquals(List.of(firstProductNote, secondProductNote), responses);
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
