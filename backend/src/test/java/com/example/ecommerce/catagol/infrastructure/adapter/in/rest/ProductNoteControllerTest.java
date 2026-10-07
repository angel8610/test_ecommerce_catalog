package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.ProductNoteCreateCommand;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProductNoteControllerTest {

  private static final String NOTE = "Durable material";
  private static final String CREATED_BY = "John Dave";
  private static final Long EXT_PROD_ID = 42L;

  @Mock
  private SaveProductNoteUseCase saveProductNoteUseCase;

  @Mock
  private FindAllProductNoteUseCase findAllProductNoteUseCase;

  private ProductNoteController productNoteController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    productNoteController = new ProductNoteController(saveProductNoteUseCase, findAllProductNoteUseCase);
    var validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = standaloneSetup(productNoteController)
      .setValidator(validator)
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }

  @Test
  void returnsOkWithSavedProductNote() {
    var productNoteRequest = this.createProductNoteRequest(2L, "NOTE");
    var productNoteCreateCommand = this.getProductNoteCreateCommandFromRequest(productNoteRequest);
    var productNote = this.createProductNote(1L, productNoteCreateCommand.extProdId(),
      productNoteCreateCommand.note());
    var productNoteResponse = new ProductNoteResponse(productNote.getNoteId(), productNote.getExtProdId(),
      productNote.getNote(), productNote.getCreatedBy());

    when(saveProductNoteUseCase.saveProductNote(productNoteCreateCommand)).thenReturn(productNote);

    ResponseEntity<ProductNoteResponse> response = productNoteController.saveProductNote(productNoteRequest);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(productNoteResponse, response.getBody());
    verify(saveProductNoteUseCase).saveProductNote(productNoteCreateCommand);
  }

  @Test
  void propagatesDuplicateExceptionWhenSavingProductNote() {
    var productNoteRequest = this.createProductNoteRequest(EXT_PROD_ID, NOTE);
    var request = this.getProductNoteCreateCommandFromRequest(productNoteRequest);
    var duplicateException = new ProductNoteDuplicateException("The Product Note is duplicate");
    when(saveProductNoteUseCase.saveProductNote(request)).thenThrow(duplicateException);

    var exception = assertThrows(ProductNoteDuplicateException.class,
      () -> productNoteController.saveProductNote(productNoteRequest));

    assertSame(duplicateException, exception);
    verify(saveProductNoteUseCase).saveProductNote(request);
  }

  @Test
  void returnsOkWithAllProductNotes() {
    var productNotes = List.of(
      new ProductNote(7L, 42L, NOTE, CREATED_BY,
        LocalDateTime.of(2024, 6, 1, 12, 0, 0)),
      new ProductNote(8L, 43L, "Easy to clean", "Jordan Lee",
        LocalDateTime.of(2024, 6, 2, 15, 30, 0))
    );
    when(findAllProductNoteUseCase.findAll()).thenReturn(productNotes);

    ResponseEntity<List<ProductNoteResponse>> response = productNoteController.findAll();

    var productsNoteResponses = List.of(
      new ProductNoteResponse(7L, 42L, NOTE, CREATED_BY),
      new ProductNoteResponse(8L, 43L, "Easy to clean", "Jordan Lee")
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(productsNoteResponses, response.getBody());
    verify(findAllProductNoteUseCase).findAll();
  }

  @Test
  void returnsOkWithEmptyListWhenUseCaseReturnsNoProductNotes() {
    when(findAllProductNoteUseCase.findAll()).thenReturn(List.of());

    ResponseEntity<List<ProductNoteResponse>> response = productNoteController.findAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(List.of(), response.getBody());
    verify(findAllProductNoteUseCase).findAll();
  }

  @Test
  void propagatesEmptyProductNoteExceptionWhenFindingNotes() {
    var emptyNotesException = new EmptyProductNoteException(
      "There are no recorded notes for the products.");
    when(findAllProductNoteUseCase.findAll()).thenThrow(emptyNotesException);

    var exception = assertThrows(EmptyProductNoteException.class, productNoteController::findAll);

    assertSame(emptyNotesException, exception);
    verify(findAllProductNoteUseCase).findAll();
  }

  @Test
  void returnsSavedProductNoteAsJsonWhenPostRequestIsValid() throws Exception {
    var productNoteRequest = this.createProductNoteRequest(EXT_PROD_ID, NOTE);
    var productNoteCreateCommand = this.getProductNoteCreateCommandFromRequest(productNoteRequest);
    var productNote = this.createProductNote(2L, EXT_PROD_ID, NOTE);

    when(saveProductNoteUseCase.saveProductNote(productNoteCreateCommand))
      .thenReturn(productNote);

    mockMvc.perform(post("/api/v1/prodnotes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "extProdId": 42,
            "note": "Durable material",
            "createdBy": "John Dave"
          }
          """))
      .andExpect(status().isCreated())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.noteId").value(2))
      .andExpect(jsonPath("$.extProdId").value(42))
      .andExpect(jsonPath("$.note").value(NOTE))
      .andExpect(jsonPath("$.createdBy").value(CREATED_BY));

    verify(saveProductNoteUseCase).saveProductNote(productNoteCreateCommand);
  }

  @Test
  void returnsBadRequestWithoutSavingWhenPostRequestIsInvalid() throws Exception {
    mockMvc.perform(post("/api/v1/prodnotes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "extProdId": null,
            "note": ""
          }
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Validation failed"))
      .andExpect(jsonPath("$.errors.length()").value(3))
      .andExpect(jsonPath("$.errors[?(@.field == 'extProdId')]").isNotEmpty())
      .andExpect(jsonPath("$.errors[?(@.field == 'note')]").isNotEmpty());

    verifyNoInteractions(saveProductNoteUseCase);
  }

  @Test
  void returnsConflictWhenPostRequestCreatesDuplicateNote() throws Exception {
    var productNoteRequest = this.createProductNoteRequest(3L, "Add notes");
    var productNoteRequestCommand = this.getProductNoteCreateCommandFromRequest(productNoteRequest);

    when(saveProductNoteUseCase.saveProductNote(org.mockito.ArgumentMatchers.any(
      ProductNoteCreateCommand.class))).thenThrow(
        new ProductNoteDuplicateException("The Product Note is duplicate"));

    mockMvc.perform(post("/api/v1/prodnotes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "extProdId": 3,
            "note": "Add notes",
            "createdBy": "user"
          }
          """))
      .andExpect(status().isConflict())
      .andExpect(content().string("The Product Note is duplicate"));

    verify(saveProductNoteUseCase).saveProductNote(productNoteRequestCommand);
  }

  @Test
  void returnsAllProductNotesAsJsonWhenGetRequestIsCalled() throws Exception {
    var productNotes = List.of(
      new ProductNote(7L, 42L, NOTE, CREATED_BY,
        LocalDateTime.of(2024, 6, 1, 12, 0, 0)),
      new ProductNote(8L, 43L, "Easy to clean", "Jordan Lee",
        LocalDateTime.of(2024, 6, 2, 15, 30, 0))
    );

    when(findAllProductNoteUseCase.findAll()).thenReturn(productNotes);

    mockMvc.perform(get("/api/v1/prodnotes"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].noteId").value(7))
      .andExpect(jsonPath("$[0].extProdId").value(42))
      .andExpect(jsonPath("$[0].note").value("Durable material"))
      .andExpect(jsonPath("$[0].createdBy").value("John Dave"))
      .andExpect(jsonPath("$[1].noteId").value(8));

    verify(findAllProductNoteUseCase).findAll();
  }

  @Test
  void returnsEmptyJsonArrayWhenGetRequestFindsNoProductNotes() throws Exception {
    when(findAllProductNoteUseCase.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/prodnotes"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());

    verify(findAllProductNoteUseCase).findAll();
  }

  @Test
  void returnsNotFoundWhenGetRequestFindsNoRecordedNotes() throws Exception {
    when(findAllProductNoteUseCase.findAll())
      .thenThrow(new EmptyProductNoteException("There are no recorded notes for the products."));

    mockMvc.perform(get("/api/v1/prodnotes"))
      .andExpect(status().isNotFound())
      .andExpect(content().string("There are no recorded notes for the products."));

    verify(findAllProductNoteUseCase).findAll();
  }

  private ProductNoteRequest createProductNoteRequest(Long expProdId, String note) {
    return new ProductNoteRequest(expProdId, note);
  }

  private ProductNoteCreateCommand getProductNoteCreateCommandFromRequest(ProductNoteRequest request) {
    return new ProductNoteCreateCommand(request.extProdId(), request.note());
  }

  private ProductNote createProductNote(Long noteId, Long extProdId, String note) {
    return ProductNote.builder()
      .noteId(noteId)
      .extProdId(extProdId)
      .note(note)
      .createdBy(ProductNoteControllerTest.CREATED_BY)
      .build();
  }


}
