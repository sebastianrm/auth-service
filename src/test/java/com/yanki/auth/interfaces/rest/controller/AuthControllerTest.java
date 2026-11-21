package com.yanki.auth.interfaces.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yanki.auth.domain.model.IssuedTokens;
import com.yanki.auth.domain.service.AuthService;
import com.yanki.auth.interfaces.rest.controller.AuthController;
import com.yanki.auth.interfaces.rest.dto.LoginRequest;
import com.yanki.auth.interfaces.rest.dto.RefreshTokenRequest;
import com.yanki.auth.interfaces.rest.dto.TokenResponse;
import com.yanki.auth.interfaces.rest.dto.TokenValidationResponse;
import com.yanki.auth.domain.model.TokenValidationResult;

import io.reactivex.rxjava3.core.Single;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(AuthController.class)
public class AuthControllerTest {

    @MockBean
    private AuthService authService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(new AuthController(authService)).build();
    }

    @Test
    void loginSuccess() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");

        IssuedTokens issuedTokens = new IssuedTokens(
            "access-token",
            "Bearer",
            900,
            "refresh-token",
            "user@example.com",
            List.of("ROLE_CUSTOMER")
        );

        TokenResponse tokenResponse = TokenResponse.from(issuedTokens);

        when(authService.login(any())).thenReturn(Single.just(issuedTokens));

        webTestClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TokenResponse.class)
            .isEqualTo(tokenResponse);
    }

    @Test
    void loginInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "invalid-password");

        when(authService.login(any())).thenReturn(Single.error(new RuntimeException("Las credenciales proporcionadas no son validas")));

        webTestClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void refreshTokenSuccess() {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("valid-refresh-token");

        IssuedTokens issuedTokens = new IssuedTokens(
            "new-access-token",
            "Bearer",
            900,
            "new-refresh-token",
            "user@example.com",
            List.of("ROLE_CUSTOMER")
        );

        TokenResponse tokenResponse = TokenResponse.from(issuedTokens);

        when(authService.refresh(any())).thenReturn(Single.just(issuedTokens));

        webTestClient.post()
            .uri("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(refreshRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TokenResponse.class)
            .isEqualTo(tokenResponse);
    }

    @Test
void validateTokenSuccess() {
    TokenValidationResult validationResult = new TokenValidationResult(true, "user@example.com", "customer-123", List.of("ROLE_CUSTOMER"));
    TokenValidationResponse validationResponse = TokenValidationResponse.from(validationResult);

    when(authService.validate(any())).thenReturn(Single.just(validationResult));

    webTestClient.get()
        .uri("/api/auth/validate")
        .header("Authorization", "Bearer valid-token")
        .exchange()
        .expectStatus().isOk()
        .expectBody(TokenValidationResponse.class)
        .isEqualTo(validationResponse);
}

    @Test
    void validateTokenMissingAuthorization() {
        webTestClient.get()
            .uri("/api/auth/validate")
            .exchange()
            .expectStatus().isBadRequest();
    }
}
