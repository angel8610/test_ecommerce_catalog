package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

  Optional<User> findByUsername(String username);


}
