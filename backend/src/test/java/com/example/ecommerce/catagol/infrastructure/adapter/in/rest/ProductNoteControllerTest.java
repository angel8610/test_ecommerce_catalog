package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import com.example.ecommerce.catagol.application.port.in.FindAllProductNoteUseCase;
import com.example.ecommerce.catagol.application.port.in.SaveProductNoteUseCase;
import com.example.ecommerce.catagol.domain.exception.EmptyProductNoteException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
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
    var request = new ProductNoteRequest(42L, "Durable material", "John Dave");
    var savedProductNote = new ProductNoteResponse(7L, 42L, "Durable material", "John Dave");
    when(saveProductNoteUseCase.saveProductNote(request)).thenReturn(savedProductNote);

    ResponseEntity<ProductNoteResponse> response = productNoteController.saveProductNote(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(savedProductNote, response.getBody());
    verify(saveProductNoteUseCase).saveProductNote(request);
  }

  @Test
  void propagatesDuplicateExceptionWhenSavingProductNote() {
    var request = new ProductNoteRequest(42L, "Durable material", "John Dave");
    var duplicateException = new ProductNoteDuplicateException("The Product Note is duplicate");
    when(saveProductNoteUseCase.saveProductNote(request)).thenThrow(duplicateException);

    var exception = assertThrows(ProductNoteDuplicateException.class,
      () -> productNoteController.saveProductNote(request));

    assertSame(duplicateException, exception);
    verify(saveProductNoteUseCase).saveProductNote(request);
  }

  @Test
  void returnsOkWithAllProductNotes() {
    var productNotes = List.of(
      new ProductNoteResponse(7L, 42L, "Durable material", "John Dave"),
      new ProductNoteResponse(8L, 43L, "Easy to clean", "Jordan Lee")
    );
    when(findAllProductNoteUseCase.findAll()).thenReturn(productNotes);

    ResponseEntity<List<ProductNoteResponse>> response = productNoteController.findAll();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(productNotes, response.getBody());
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
    var request = new ProductNoteRequest(42L, "Durable material", "John Dave");
    when(saveProductNoteUseCase.saveProductNote(request))
      .thenReturn(new ProductNoteResponse(7L, 42L, "Durable material", "John Dave"));

    mockMvc.perform(post("/api/v1/prodnotes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "extProdId": 42,
            "note": "Durable material",
            "createdBy": "John Dave"
          }
          """))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.noteId").value(7))
      .andExpect(jsonPath("$.extProdId").value(42))
      .andExpect(jsonPath("$.note").value("Durable material"))
      .andExpect(jsonPath("$.createdBy").value("John Dave"));

    verify(saveProductNoteUseCase).saveProductNote(request);
  }

  @Test
  void returnsBadRequestWithoutSavingWhenPostRequestIsInvalid() throws Exception {
    mockMvc.perform(post("/api/v1/prodnotes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "extProdId": null,
            "note": "",
            "createdBy": "John123"
          }
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Validation failed"))
      .andExpect(jsonPath("$.errors.length()").value(4))
      .andExpect(jsonPath("$.errors[?(@.field == 'extProdId')]").isNotEmpty())
      .andExpect(jsonPath("$.errors[?(@.field == 'note')]").isNotEmpty())
      .andExpect(jsonPath("$.errors[?(@.field == 'createdBy')]").isNotEmpty());

    verifyNoInteractions(saveProductNoteUseCase);
  }

  @Test
  void returnsConflictWhenPostRequestCreatesDuplicateNote() throws Exception {
    when(saveProductNoteUseCase.saveProductNote(org.mockito.ArgumentMatchers.any(ProductNoteRequest.class)))
      .thenThrow(new ProductNoteDuplicateException("The Product Note is duplicate"));

    mockMvc.perform(post("/api/v1/prodnotes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "extProdId": 42,
            "note": "Durable material",
            "createdBy": "John Dave"
          }
          """))
      .andExpect(status().isConflict())
      .andExpect(content().string("The Product Note is duplicate"));

    verify(saveProductNoteUseCase).saveProductNote(
      new ProductNoteRequest(42L, "Durable material", "John Dave")
    );
  }

  @Test
  void returnsAllProductNotesAsJsonWhenGetRequestIsCalled() throws Exception {
    when(findAllProductNoteUseCase.findAll()).thenReturn(List.of(
      new ProductNoteResponse(7L, 42L, "Durable material", "John Dave"),
      new ProductNoteResponse(8L, 43L, "Easy to clean", "Jordan Lee")
    ));

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


}
