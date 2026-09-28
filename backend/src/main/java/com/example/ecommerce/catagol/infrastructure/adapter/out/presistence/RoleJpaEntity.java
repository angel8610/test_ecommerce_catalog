package com.example.ecommerce.catagol.infrastructure.adapter.out.presistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long roleId;

  @Column(name = "role_name")
  private String name;

  private String description;

  @Column(name = "user_id")
  private Long userId;



}
