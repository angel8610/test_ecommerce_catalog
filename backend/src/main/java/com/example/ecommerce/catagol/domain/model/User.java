package com.example.ecommerce.catagol.domain.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

  @EqualsAndHashCode.Include
  private Long id;

  private String username;

  private String password;

  private List<Role> roles;

  private String firstName;

  private String lastName;


}
