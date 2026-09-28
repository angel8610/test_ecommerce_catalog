package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.LoginResponse;

public interface AuthenticateUserUseCase {

  LoginResponse authenticate(LoginRequest request);


}
