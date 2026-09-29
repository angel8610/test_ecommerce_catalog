package com.example.ecommerce.catagol.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

  @EqualsAndHashCode.Include
  private Long roleId;

  private String name;

  private String description;

  private Long userId;


}
