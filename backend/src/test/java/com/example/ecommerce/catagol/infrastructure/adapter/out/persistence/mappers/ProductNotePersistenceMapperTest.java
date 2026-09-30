package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.ProductNoteJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductNotePersistenceMapperTest {

  private final ProductNotePersistenceMapper mapper = new ProductNotePersistenceMapper();

  @Test
  void mapsProductNoteToJpaEntity() {
    var updatedAt = LocalDateTime.of(2026, 9, 29, 15, 0);
    var productNote = ProductNote.builder()
      .noteId(7L)
      .extProdId(42L)
      .note("Durable material")
      .createdBy("John Dave")
      .updatedAt(updatedAt)
      .build();

    ProductNoteJpaEntity entity = mapper.mapToEntity(productNote);

    assertEquals(7L, entity.getNoteId());
    assertEquals(42L, entity.getExtProdId());
    assertEquals("Durable material", entity.getNote());
    assertEquals("John Dave", entity.getCreatedBy());
    assertEquals(updatedAt, entity.getUpdatedAt());
  }

  @Test
  void returnsNullWhenMappingNullProductNoteToEntity() {
    assertNull(mapper.mapToEntity(null));
  }

  @Test
  void mapsJpaEntityToProductNote() {
    var updatedAt = LocalDateTime.of(2026, 9, 29, 15, 0);
    var entity = ProductNoteJpaEntity.builder()
      .noteId(7L)
      .extProdId(42L)
      .note("Durable material")
      .createdBy("John Dave")
      .updatedAt(updatedAt)
      .build();

    ProductNote productNote = mapper.mapToDomain(entity);

    assertEquals(7L, productNote.getNoteId());
    assertEquals(42L, productNote.getExtProdId());
    assertEquals("Durable material", productNote.getNote());
    assertEquals("John Dave", productNote.getCreatedBy());
    assertEquals(updatedAt, productNote.getUpdatedAt());
  }

  @Test
  void returnsNullWhenMappingNullEntityToDomain() {
    assertNull(mapper.mapToDomain(null));
  }


}
