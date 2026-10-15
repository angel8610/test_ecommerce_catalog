package com.example.ecommerce.catagol.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.refresh")
@Getter
@Setter
public class SecurityRefreshConfig {

  private Long expirationDays;


}
