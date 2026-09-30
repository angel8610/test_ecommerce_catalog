package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.ProductNoteJpaEntity;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.ProductNotePersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpringDataProductNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProductNoteRepositoryAdapterTest {

  @Mock
  private SpringDataProductNoteRepository springDataProductNoteRepository;

  private JpaProductNoteRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new JpaProductNoteRepositoryAdapter(springDataProductNoteRepository,
      new ProductNotePersistenceMapper());
  }

  @Test
  void savesProductNoteAndReturnsMappedSavedEntity() {
    var updatedAt = LocalDateTime.of(2026, 9, 29, 15, 0);
    var productNote = createProductNote(null, 42L, "Durable material", "John Dave", updatedAt);
    when(springDataProductNoteRepository.save(any(ProductNoteJpaEntity.class)))
      .thenAnswer(invocation -> {
        ProductNoteJpaEntity entity = invocation.getArgument(0);
        entity.setNoteId(7L);
        return entity;
      });

    ProductNote savedProductNote = adapter.save(productNote);

    assertEquals(7L, savedProductNote.getNoteId());
    assertEquals(42L, savedProductNote.getExtProdId());
    assertEquals("Durable material", savedProductNote.getNote());
    assertEquals("John Dave", savedProductNote.getCreatedBy());
    assertEquals(updatedAt, savedProductNote.getUpdatedAt());

    var entityCaptor = ArgumentCaptor.forClass(ProductNoteJpaEntity.class);
    verify(springDataProductNoteRepository).save(entityCaptor.capture());
    var persistedEntity = entityCaptor.getValue();
    assertEquals(42L, persistedEntity.getExtProdId());
    assertEquals("Durable material", persistedEntity.getNote());
    assertEquals("John Dave", persistedEntity.getCreatedBy());
    assertEquals(updatedAt, persistedEntity.getUpdatedAt());
  }

  @Test
  void propagatesRepositoryErrorsWhenSavingProductNote() {
    var productNote = createProductNote(null, 42L, "Durable material",
      "John Dave", null);
    var repositoryException = new IllegalStateException("Product note database unavailable");
    when(springDataProductNoteRepository.save(any(ProductNoteJpaEntity.class)))
      .thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class, () -> adapter.save(productNote));

    assertSame(repositoryException, exception);
    verify(springDataProductNoteRepository).save(any(ProductNoteJpaEntity.class));
  }

  @Test
  void returnsAllProductNotesMappedInRepositoryOrder() {
    var firstEntity = createProductNoteEntity(1L, 42L, "Durable material",
      "John Dave", LocalDateTime.of(2026, 9, 28, 10, 0));
    var secondEntity = createProductNoteEntity(2L, 43L, "Easy to clean",
      "Jordan Lee", LocalDateTime.of(2026, 9, 29, 11, 30));
    when(springDataProductNoteRepository.findAll()).thenReturn(List.of(firstEntity, secondEntity));

    List<ProductNote> productNotes = adapter.findAll();

    assertEquals(
      List.of(
        createProductNote(1L, 42L, "Durable material", "John Dave",
          LocalDateTime.of(2026, 9, 28, 10, 0)),
        createProductNote(2L, 43L, "Easy to clean", "Jordan Lee",
          LocalDateTime.of(2026, 9, 29, 11, 30))
      ),
      productNotes
    );
    verify(springDataProductNoteRepository).findAll();
  }

  @Test
  void returnsEmptyListWhenRepositoryHasNoProductNotes() {
    when(springDataProductNoteRepository.findAll()).thenReturn(List.of());

    List<ProductNote> productNotes = adapter.findAll();

    assertEquals(List.of(), productNotes);
    verify(springDataProductNoteRepository).findAll();
  }

  @Test
  void propagatesRepositoryErrorsWhenFindingProductNotes() {
    var repositoryException = new IllegalStateException("Product note database unavailable");
    when(springDataProductNoteRepository.findAll()).thenThrow(repositoryException);

    var exception = assertThrows(IllegalStateException.class, adapter::findAll);

    assertSame(repositoryException, exception);
    verify(springDataProductNoteRepository).findAll();
  }

  private ProductNote createProductNote(
    Long noteId,
    Long extProdId,
    String note,
    String createdBy,
    LocalDateTime updatedAt
  ) {
    return ProductNote.builder()
      .noteId(noteId)
      .extProdId(extProdId)
      .note(note)
      .createdBy(createdBy)
      .updatedAt(updatedAt)
      .build();
  }

  private ProductNoteJpaEntity createProductNoteEntity(
    Long noteId,
    Long extProdId,
    String note,
    String createdBy,
    LocalDateTime updatedAt
  ) {
    return ProductNoteJpaEntity.builder()
      .noteId(noteId)
      .extProdId(extProdId)
      .note(note)
      .createdBy(createdBy)
      .updatedAt(updatedAt)
      .build();
  }


}
