package com.accountabilityatlas.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

class CorsConfigTest {

  private CorsConfig corsConfig;

  @BeforeEach
  void setUp() throws Exception {
    corsConfig = new CorsConfig();
    setAllowedOrigins("http://localhost:3000", "http://example.com");
  }

  @Test
  void corsWebFilter_returnsNonNullFilter() {
    CorsWebFilter filter = corsConfig.corsWebFilter();

    assertThat(filter).isNotNull();
  }

  @Test
  void corsWebFilter_createsFilterWithCorrectType() {
    CorsWebFilter filter = corsConfig.corsWebFilter();

    assertThat(filter).isInstanceOf(CorsWebFilter.class);
  }

  @Test
  void corsWebFilter_withSingleOrigin_returnsFilter() throws Exception {
    setAllowedOrigins("http://localhost:3000");

    CorsWebFilter filter = corsConfig.corsWebFilter();

    assertThat(filter).isNotNull();
  }

  @Test
  void corsWebFilter_withMultipleOrigins_returnsFilter() throws Exception {
    setAllowedOrigins("http://localhost:3000", "http://localhost:8080", "https://example.com");

    CorsWebFilter filter = corsConfig.corsWebFilter();

    assertThat(filter).isNotNull();
  }

  private void setAllowedOrigins(String... origins) throws Exception {
    Field field = CorsConfig.class.getDeclaredField("allowedOrigins");
    field.setAccessible(true);
    field.set(corsConfig, origins);
  }
}
