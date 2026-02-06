package com.accountabilityatlas.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final List<String> PUBLIC_PATHS = List.of("/api/v1/auth/");

  private final KeyPair keyPair;

  public JwtAuthenticationGatewayFilter(KeyPair keyPair) {
    this.keyPair = keyPair;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    // Skip authentication for public paths
    if (isPublicPath(path)) {
      return chain.filter(exchange);
    }

    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return handleUnauthorized(exchange, "MISSING_TOKEN", "Authorization token is required");
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    try {
      Claims claims = parseToken(token);

      // Add user context headers for downstream services
      ServerHttpRequest modifiedRequest =
          request
              .mutate()
              .header("X-User-Id", claims.getSubject())
              .header("X-User-Email", claims.get("email", String.class))
              .header("X-Trust-Tier", claims.get("trustTier", String.class))
              .build();

      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    } catch (ExpiredJwtException e) {
      return handleUnauthorized(exchange, "TOKEN_EXPIRED", "Access token expired");
    } catch (JwtException | IllegalArgumentException e) {
      return handleUnauthorized(exchange, "INVALID_TOKEN", "Invalid access token");
    }
  }

  private boolean isPublicPath(String path) {
    return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
  }

  private Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(keyPair.getPublic())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String code, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    String body = String.format("{\"code\":\"%s\",\"message\":\"%s\"}", code, message);
    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

    return response.writeWith(Mono.just(buffer));
  }

  @Override
  public int getOrder() {
    return -100; // Run early in the filter chain
  }
}
