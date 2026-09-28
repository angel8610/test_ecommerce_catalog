package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.domain.model.AuthenticatedUser;

public interface TokenProviderPort {

  String generateToken(AuthenticatedUser user);


}
