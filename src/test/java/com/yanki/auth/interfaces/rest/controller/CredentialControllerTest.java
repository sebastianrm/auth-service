package com.yanki.auth.interfaces.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yanki.auth.domain.model.CredentialCreateCommand;
import com.yanki.auth.domain.model.CredentialUpdateCommand;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.service.CredentialService;
import com.yanki.auth.interfaces.rest.dto.CredentialCreateRequest;
import com.yanki.auth.interfaces.rest.dto.CredentialResponse;
import com.yanki.auth.interfaces.rest.dto.CredentialUpdateRequest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(CredentialController.class)
public class CredentialControllerTest {

    @MockBean
    private CredentialService credentialService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(new CredentialController(credentialService)).build();
    }

    @Test
void createCredentialSuccess() {
    Credential credential = Credential.builder()
        .id("1")
        .username("user@example.com")
        .passwordHash("encodedPassword")
        .build();
    CredentialCreateRequest request = new CredentialCreateRequest(
        "user@example.com", 
        "password", 
        "customerId",
        "user@example.com", 
        List.of("ROLE_CUSTOMER"), 
        true
    );

    when(credentialService.create(any(CredentialCreateCommand.class))).thenReturn(Single.just(credential));

    webTestClient.post()
        .uri("/api/auth/credentials")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(CredentialResponse.class)
        .value(response -> response.username().equals("user@example.com"));
}

    @Test
    void findAllCredentialsSuccess() {
Credential credential1 = Credential.builder()
    .id("1")
    .username("user1@example.com")
    .passwordHash("encodedPassword")
    .build();
Credential credential2 = Credential.builder()
    .id("2")
    .username("user2@example.com")
    .passwordHash("encodedPassword")
    .build();

        when(credentialService.findAll()).thenReturn(Flowable.just(credential1, credential2));

        webTestClient.get()
            .uri("/api/auth/credentials")
            .exchange()
            .expectStatus().isOk()
.expectBodyList(CredentialResponse.class)
.hasSize(2);
    }

    @Test
    void findCredentialByIdSuccess() {
Credential credential = Credential.builder()
    .id("1")
    .username("user@example.com")
    .passwordHash("encodedPassword")
    .build();

when(credentialService.findById(anyString())).thenReturn(Maybe.just(credential));

        webTestClient.get()
            .uri("/api/auth/credentials/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(CredentialResponse.class)
            .value(response -> response.username().equals("user@example.com"));
    }

    @Test
void updateCredentialSuccess() {
    Credential credential = Credential.builder()
        .id("1")
        .username("user@example.com")
        .passwordHash("encodedPassword")
        .build();
    CredentialUpdateRequest request = new CredentialUpdateRequest(
        null, 
        "customerId", 
        "newUsername@example.com", 
        null, 
        true
    );

    when(credentialService.update(anyString(), any(CredentialUpdateCommand.class))).thenReturn(Single.just(credential));

    webTestClient.put()
        .uri("/api/auth/credentials/1")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk()
        .expectBody(CredentialResponse.class)
        .value(response -> response.username().equals("user@example.com"));
}

    @Test
    void deleteCredentialSuccess() {
        when(credentialService.delete(anyString())).thenReturn(Completable.complete());

        webTestClient.delete()
            .uri("/api/auth/credentials/1")
            .exchange()
            .expectStatus().isNoContent();
    }
}
