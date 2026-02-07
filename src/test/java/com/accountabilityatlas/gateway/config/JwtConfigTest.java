package com.accountabilityatlas.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtConfigTest {

  private JwtConfig jwtConfig;

  @BeforeEach
  void setUp() {
    jwtConfig = new JwtConfig();
  }

  @Test
  void jwtKeyPair_returnsNonNullKeyPair() throws Exception {
    KeyPair keyPair = jwtConfig.jwtKeyPair();

    assertThat(keyPair).isNotNull();
  }

  @Test
  void jwtKeyPair_returnsRsaPublicKey() throws Exception {
    KeyPair keyPair = jwtConfig.jwtKeyPair();

    assertThat(keyPair.getPublic()).isInstanceOf(RSAPublicKey.class);
  }

  @Test
  void jwtKeyPair_returnsRsaPrivateKey() throws Exception {
    KeyPair keyPair = jwtConfig.jwtKeyPair();

    assertThat(keyPair.getPrivate()).isInstanceOf(RSAPrivateKey.class);
  }

  @Test
  void jwtKeyPair_returns2048BitKey() throws Exception {
    KeyPair keyPair = jwtConfig.jwtKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

    assertThat(publicKey.getModulus().bitLength()).isEqualTo(2048);
  }

  @Test
  void jwtKeyPair_generatesUniqueKeyPairsOnEachCall() throws Exception {
    KeyPair keyPair1 = jwtConfig.jwtKeyPair();
    KeyPair keyPair2 = jwtConfig.jwtKeyPair();

    assertThat(keyPair1.getPublic()).isNotEqualTo(keyPair2.getPublic());
    assertThat(keyPair1.getPrivate()).isNotEqualTo(keyPair2.getPrivate());
  }
}
