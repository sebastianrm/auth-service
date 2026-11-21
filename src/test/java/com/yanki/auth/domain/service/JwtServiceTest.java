package com.yanki.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.TokenClaims;
import com.yanki.auth.infrastructure.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de tokens JWT.
 */
class JwtServiceTest {

    private static final String SECRET = "yanki-test-secret-key-with-at-least-32-bytes";
    private static final String OTHER_SECRET = "another-test-secret-key-with-at-least-32-bytes";
    private static final String ISSUER = "yanki-auth-service";
    private static final String USERNAME = "john.doe";
    private static final String CUSTOMER_ID = "CUSTOMER-1";

    private JwtProperties jwtProperties;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setIssuer(ISSUER);
        jwtProperties.setAccessTokenExpiration(Duration.ofHours(1));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void shouldIncludeExpectedClaimsInGeneratedAccessToken() {
        String token = jwtService.generateAccessToken(credential());

        TokenClaims claims = jwtService.parseToken(token, JwtService.TYPE_ACCESS);

        assertThat(claims.username()).isEqualTo(USERNAME);
        assertThat(claims.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(claims.roles()).containsExactly("ROLE_CUSTOMER");
        assertThat(claims.type()).isEqualTo(JwtService.TYPE_ACCESS);
        assertThat(claims.expiresAt()).isAfter(claims.issuedAt());
    }

    @Test
    void shouldSignTokensWithHs256Algorithm() {
        String token = jwtService.generateAccessToken(credential());

        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);

        assertThat(header).contains("\"alg\":\"HS256\"");
    }

    @Test
    void shouldIssueRefreshTokenWithRefreshType() {
        String token = jwtService.generateRefreshToken(credential());

        TokenClaims claims = jwtService.parseToken(token, JwtService.TYPE_REFRESH);

        assertThat(claims.type()).isEqualTo(JwtService.TYPE_REFRESH);
        assertThat(claims.username()).isEqualTo(USERNAME);
        assertThat(claims.expiresAt()).isAfter(claims.issuedAt());
    }

    @Test
    void shouldRejectExpiredToken() {
        jwtProperties.setAccessTokenExpiration(Duration.ofSeconds(-30));
        String expiredToken = jwtService.generateAccessToken(credential());

        BusinessException error = assertThrows(BusinessException.class,
                () -> jwtService.parseToken(expiredToken, JwtService.TYPE_ACCESS));

        assertThat(error.getCode()).isEqualTo("TOKEN_EXPIRED");
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setSecret(OTHER_SECRET);
        otherProperties.setIssuer(ISSUER);
        JwtService otherService = new JwtService(otherProperties);
        String foreignToken = otherService.generateAccessToken(credential());

        BusinessException error = assertThrows(BusinessException.class,
                () -> jwtService.parseToken(foreignToken, JwtService.TYPE_ACCESS));

        assertThat(error.getCode()).isEqualTo("INVALID_TOKEN");
    }

    @Test
    void shouldRejectAccessTokenWhenRefreshTokenIsExpected() {
        String accessToken = jwtService.generateAccessToken(credential());

        BusinessException error = assertThrows(BusinessException.class,
                () -> jwtService.parseToken(accessToken, JwtService.TYPE_REFRESH));

        assertThat(error.getCode()).isEqualTo("INVALID_TOKEN");
    }

    @Test
    void shouldRejectSecretShorterThanThirtyTwoBytes() {
        JwtProperties shortProperties = new JwtProperties();
        shortProperties.setSecret("short-secret");

        assertThrows(IllegalStateException.class, () -> new JwtService(shortProperties));
    }

    private Credential credential() {
        return Credential.builder()
                .id("CREDENTIAL-1")
                .username(USERNAME)
                .passwordHash("$2a$10$hashedPassword")
                .customerId(CUSTOMER_ID)
                .email("john.doe@yanki.pe")
                .roles(List.of("ROLE_CUSTOMER"))
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
