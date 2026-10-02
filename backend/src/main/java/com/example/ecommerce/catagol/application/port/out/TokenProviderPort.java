package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.application.model.AuthenticatedUser;

public interface TokenProviderPort {

  String generateToken(AuthenticatedUser user);


}
