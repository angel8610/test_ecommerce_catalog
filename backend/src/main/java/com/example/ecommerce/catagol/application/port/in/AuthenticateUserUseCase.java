package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.application.model.Login;

public interface AuthenticateUserUseCase {

  Login authenticate(LoginCommand loginCommand);


}
