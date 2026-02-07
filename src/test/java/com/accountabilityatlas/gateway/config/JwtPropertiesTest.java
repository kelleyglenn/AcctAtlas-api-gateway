package com.accountabilityatlas.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

  private JwtProperties jwtProperties;

  @BeforeEach
  void setUp() {
    jwtProperties = new JwtProperties();
  }

  @Test
  void publicKeyPath_defaultsToNull() {
    assertThat(jwtProperties.getPublicKeyPath()).isNull();
  }

  @Test
  void setPublicKeyPath_setsValue() {
    String path = "/path/to/public-key.pem";

    jwtProperties.setPublicKeyPath(path);

    assertThat(jwtProperties.getPublicKeyPath()).isEqualTo(path);
  }

  @Test
  void setPublicKeyPath_canBeSetToNull() {
    jwtProperties.setPublicKeyPath("/some/path");

    jwtProperties.setPublicKeyPath(null);

    assertThat(jwtProperties.getPublicKeyPath()).isNull();
  }

  @Test
  void setPublicKeyPath_canBeSetToEmptyString() {
    jwtProperties.setPublicKeyPath("");

    assertThat(jwtProperties.getPublicKeyPath()).isEmpty();
  }
}
