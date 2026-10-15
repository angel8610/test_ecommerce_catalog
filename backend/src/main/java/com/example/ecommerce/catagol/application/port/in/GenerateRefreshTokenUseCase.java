package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.application.model.AuthRefreshToken;

public interface GenerateRefreshTokenUseCase {

  AuthRefreshToken generateRefreshToken(String username);


}
