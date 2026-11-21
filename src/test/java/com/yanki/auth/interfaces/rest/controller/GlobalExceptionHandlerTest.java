package com.yanki.auth.interfaces.rest.controller;

import static org.mockito.Mockito.when;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.service.AuthService;
import com.yanki.auth.interfaces.rest.dto.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(AuthController.class)
public class GlobalExceptionHandlerTest {

    @MockBean
    private AuthService authService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(new AuthController(authService)).build();
    }

    @Test
    void handleBusinessException() {
        when(authService.login(null)).thenThrow(new BusinessException("INVALID_CREDENTIALS", "Invalid credentials"));

        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody(ApiError.class)
.consumeWith(response -> {
    ApiError apiError = response.getResponseBody();
    assert apiError.code().equals("INVALID_CREDENTIALS");
    assert apiError.message().equals("Invalid credentials");
});
    }

    @Test
    void handleGenericException() {
        when(authService.login(null)).thenThrow(new RuntimeException("Internal error"));

        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(ApiError.class)
.consumeWith(response -> {
    ApiError apiError = response.getResponseBody();
    assert apiError.message().contains("Internal error");
});
    }
}
