package com.example.ecommerce.catagol.infrastructure.adapter.out.presistence;

import com.example.ecommerce.catagol.application.port.out.UserRepositoryPort;
import com.example.ecommerce.catagol.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

  private final SpingDataUserRepository spingDataUserRepository;
  private final UserPersistenceMapper userPersistenceMapper;

  public JpaUserRepositoryAdapter(SpingDataUserRepository spingDataUserRepository,
                                  UserPersistenceMapper userPersistenceMapper) {
    this.spingDataUserRepository = spingDataUserRepository;
    this.userPersistenceMapper = userPersistenceMapper;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return spingDataUserRepository.findByUsername(username)
            .map(this.userPersistenceMapper::mapToDomain);
  }



}
