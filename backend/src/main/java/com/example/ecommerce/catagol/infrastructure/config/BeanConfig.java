package com.example.ecommerce.catagol.infrastructure.config;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.AuditLogRepositoryPort;
import com.example.ecommerce.catagol.application.port.out.ExternalCatalogPort;
import com.example.ecommerce.catagol.application.port.out.TokenProviderPort;
import com.example.ecommerce.catagol.application.port.out.UserRepositoryPort;
import com.example.ecommerce.catagol.application.service.AuthenticationService;
import com.example.ecommerce.catagol.application.service.ProductService;
import com.example.ecommerce.catagol.infrastructure.adapter.out.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
public class BeanConfig {

  @Bean
  public CustomUserDetailsService customUserDetailsService(UserRepositoryPort userRepositoryPort) {
    return new CustomUserDetailsService(userRepositoryPort);
  }

  @Bean
  public GetProductUseCase productUseCase(ExternalCatalogPort externalCatalogPort,
                                          AuditLogRepositoryPort auditLogRepositoryPort) {
    return new ProductService(externalCatalogPort, auditLogRepositoryPort);
  }

  @Bean
  public AuthenticationService authenticationService(AuthenticationManager authenticationManager,
                                                     TokenProviderPort tokenProvider,
                                                     SecurityJwtConfig securityJwtConfig) {
    return new AuthenticationService(authenticationManager, tokenProvider, securityJwtConfig);
  }


}
