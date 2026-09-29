package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers;

import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.ProductNoteJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductNotePersistenceMapper {

  public ProductNoteJpaEntity mapToEntity(ProductNote productNote) {
    if(productNote == null) {
      return null;
    }

    return ProductNoteJpaEntity.builder()
      .createdBy(productNote.getCreatedBy())
      .extProdId(productNote.getExtProdId())
      .noteId(productNote.getNoteId())
      .note(productNote.getNote())
      .updatedAt(productNote.getUpdatedAt())
      .build();
  }

  public ProductNote mapToDomain(ProductNoteJpaEntity productNoteJpaEntity) {
    if(productNoteJpaEntity == null) {
      return null;
    }

    return ProductNote.builder()
      .createdBy(productNoteJpaEntity.getCreatedBy())
      .extProdId(productNoteJpaEntity.getExtProdId())
      .note(productNoteJpaEntity.getNote())
      .noteId(productNoteJpaEntity.getNoteId())
      .updatedAt(productNoteJpaEntity.getUpdatedAt())
      .build();
  }


}
