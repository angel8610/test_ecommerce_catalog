package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence;

import com.example.ecommerce.catagol.application.port.out.ProductNoteRepositoryPort;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.mappers.ProductNotePersistenceMapper;
import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories.SpringDataProductNoteRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductNoteRepositoryAdapter implements ProductNoteRepositoryPort {

  private final SpringDataProductNoteRepository springDataProductNoteRepository;
  private final ProductNotePersistenceMapper productNotePersistenceMapper;

  public JpaProductNoteRepositoryAdapter(SpringDataProductNoteRepository springDataProductNoteRepository,
                                         ProductNotePersistenceMapper productNotePersistenceMapper) {
    this.springDataProductNoteRepository = springDataProductNoteRepository;
    this.productNotePersistenceMapper = productNotePersistenceMapper;
  }

  @Override
  public ProductNote save(ProductNote productNote) {
    var productNoteEntity = this.springDataProductNoteRepository.save(
      this.productNotePersistenceMapper.mapToEntity(productNote));
    return this.productNotePersistenceMapper.mapToDomain(productNoteEntity);
  }


}
