package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalogs")
public class AuthTestController {

  @GetMapping(path = "/all")
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ResponseEntity<String> getAllCatalogs() {
    return ResponseEntity.ok("Access granted to all catalogs");
  }

  @GetMapping(path = "/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> getAdmin() {
    return ResponseEntity.ok("Access granted to all catalogs for ADMIN");
  }

  @GetMapping(path = "/user")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> getUser() {
    return ResponseEntity.ok("Access granted to all catalogs for USER");
  }


}
