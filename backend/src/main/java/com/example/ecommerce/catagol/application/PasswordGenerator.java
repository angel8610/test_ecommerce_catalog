package com.example.ecommerce.catagol.application;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {

  public static void main(String[] args) {

    PasswordEncoder encoder = new BCryptPasswordEncoder(12);

    String password = "UserABC#!";

    String passwordHash = encoder.encode(password);

    System.out.println("BCrypt: " + passwordHash);

    System.out.println(
      "Validación: " + encoder.matches(password, passwordHash)
    );
  }
}
