package com.example.ecommerce.catagol.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class SecurityJwtConfig {

  private String issuer;
  private Long expiration;
  private String secret;


}
