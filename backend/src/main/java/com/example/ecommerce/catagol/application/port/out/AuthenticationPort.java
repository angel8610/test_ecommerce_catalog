package com.example.ecommerce.catagol.application.port.out;

import java.util.Optional;

public interface AuthenticationPort {

  Optional<String> getAuthenticatedUsername();

  boolean hasRole(String role);


}
