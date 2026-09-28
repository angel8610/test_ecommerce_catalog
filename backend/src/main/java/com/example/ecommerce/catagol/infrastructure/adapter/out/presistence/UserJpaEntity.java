package com.example.ecommerce.catagol.infrastructure.adapter.out.presistence;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Column(nullable = false)
  private String username;

  private String password;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private List<RoleJpaEntity> roles;


}
