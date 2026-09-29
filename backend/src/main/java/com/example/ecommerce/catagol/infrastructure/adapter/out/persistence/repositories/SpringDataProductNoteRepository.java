package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories;

import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.ProductNoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductNoteRepository extends JpaRepository<ProductNoteJpaEntity, Long> {


}
