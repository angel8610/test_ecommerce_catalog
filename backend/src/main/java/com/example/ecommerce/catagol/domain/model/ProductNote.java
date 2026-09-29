package com.example.ecommerce.catagol.domain.model;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductNote {

  @EqualsAndHashCode.Include
  private Long noteId;

  private Long extProdId;

  private String note;

  private String createdBy;

  private LocalDateTime updatedAt;


}
