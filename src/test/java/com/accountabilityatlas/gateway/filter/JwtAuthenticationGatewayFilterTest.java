package com.accountabilityatlas.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationGatewayFilterTest {

  private JwtAuthenticationGatewayFilter filter;
  private KeyPair keyPair;
  private GatewayFilterChain chain;

  @BeforeEach
  void setUp() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    keyPair = generator.generateKeyPair();
    // Test with passthrough mode disabled to verify JWT validation
    filter = new JwtAuthenticationGatewayFilter(keyPair, false);
    chain = mock(GatewayFilterChain.class);
  }

  @Test
  void filter_publicPath_passesThrough() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(chain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
  }

  @Test
  void filter_missingToken_returns401() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void filter_invalidToken_returns401() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/v1/users/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void filter_expiredToken_returns401() {
    String expiredToken = createToken(UUID.randomUUID(), "test@example.com", "NEW", -3600);
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/v1/users/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void filter_validToken_passesThrough() {
    UUID userId = UUID.randomUUID();
    String email = "test@example.com";
    String trustTier = "NEW";
    String validToken = createToken(userId, email, trustTier, 3600);

    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/v1/users/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    // Status should not be set (filter passed through to chain)
    assertThat(exchange.getResponse().getStatusCode()).isNull();
  }

  private String createToken(UUID userId, String email, String trustTier, int expirySeconds) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .claim("trustTier", trustTier)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expirySeconds)))
        .signWith(keyPair.getPrivate())
        .compact();
  }
}
