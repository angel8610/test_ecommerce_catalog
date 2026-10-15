package com.example.ecommerce.catagol.application.port.out;

public interface RefreshTokenGeneratorPort {

  String generateRawToken();

  String hash(String rawToken);


}
