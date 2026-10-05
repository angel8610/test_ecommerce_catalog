package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities;

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

  @Column(nullable = false, length = 50)
  private String username;

  @Column(nullable = false, length = 500)
  private String password;

  @Column(nullable = false, length = 100)
  private String firstName;

  @Column(nullable = false, length = 50)
  private String lastName;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private List<RoleJpaEntity> roles;


}
