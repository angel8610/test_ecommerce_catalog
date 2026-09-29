package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductNoteJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long noteId;

  @Column(name = "ext_prod_id", nullable = false, unique = true)
  private Long extProdId;

  @Column(name = "note_content", nullable = false, length = 1000)
  private String note;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }


}
