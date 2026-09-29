package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.repositories;

import com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpingDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

  Optional<UserJpaEntity> findByUsername(String username);

}
