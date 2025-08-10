package com.myce.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PortOneConfig {
  @Value("${port-one.base-url}")
  private String baseUrl;

  @Value("${port-one.api-key}")
  private String apiKey;

  @Value("${port-one.api-secret}")
  private String apiSecret;

  @Value("${port-one.customer-code}")
  private String customerCode;
}
