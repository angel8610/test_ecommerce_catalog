package com.example.ecommerce.catagol.infrastructure.config;

import com.example.ecommerce.catagol.application.port.in.GetProductUseCase;
import com.example.ecommerce.catagol.application.port.out.*;
import com.example.ecommerce.catagol.application.service.*;
import com.example.ecommerce.catagol.infrastructure.adapter.out.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.Clock;

@Configuration
public class BeanConfig {

  @Bean
  public CustomUserDetailsService customUserDetailsService(UserRepositoryPort userRepositoryPort) {
    return new CustomUserDetailsService(userRepositoryPort);
  }

  @Bean
  public GetProductUseCase productUseCase(ExternalCatalogPort externalCatalogPort,
                                          ProductNoteRepositoryPort productNoteRepositoryPort) {
    return new ProductService(externalCatalogPort, productNoteRepositoryPort);
  }

  @Bean
  public AuthenticationService authenticationService(AuthenticationManager authenticationManager,
                                                     TokenProviderPort tokenProvider,
                                                     SecurityJwtConfig securityJwtConfig) {
    return new AuthenticationService(authenticationManager, tokenProvider, securityJwtConfig);
  }

  @Bean
  public ProductNoteService productNoteService(ProductNoteRepositoryPort productNoteRepositoryPort,
                                               AuthenticationPort authenticationPort) {
    return new ProductNoteService(productNoteRepositoryPort, authenticationPort);
  }

  @Bean
  public AuditLogService auditLogService(AuditLogRepositoryPort auditLogRepositoryPort) {
    return new AuditLogService(auditLogRepositoryPort);
  }

  @Bean
  public RefreshTokenService refreshTokenService(Clock clock, RefreshTokenPort refreshTokenPort,
                                                 RefreshTokenGeneratorPort refreshTokenGeneratorPort,
                                                 UserRepositoryPort userRepositoryPort,
                                                 TokenProviderPort tokenProviderPort,
                                                 SecurityRefreshConfig securityRefreshConfig) {
    return new RefreshTokenService(clock, refreshTokenPort, refreshTokenGeneratorPort, userRepositoryPort,
      tokenProviderPort, securityRefreshConfig);
  }


}
