package com.yanki.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.CredentialCreateCommand;
import com.yanki.auth.domain.model.CredentialUpdateCommand;
import com.yanki.auth.domain.repository.CredentialRepository;
import com.yanki.auth.domain.repository.ProcessedEventRepository;
import io.reactivex.rxjava3.core.Completable;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Pruebas unitarias del servicio de credenciales.
 */
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    private static final String USERNAME = "john.doe";
    private static final String CUSTOMER_ID = "CUSTOMER-1";

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private CredentialCacheService credentialCacheService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private CredentialService credentialService;

    @BeforeEach
    void setUp() {
        credentialService = new CredentialService(
                credentialRepository, credentialCacheService, passwordEncoder, processedEventRepository);
    }

    @Test
    void shouldHashPasswordAndInvalidateCacheWhenCreatingCredential() {
        when(passwordEncoder.encode("Secret-123")).thenReturn("hashed-password");
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(credentialCacheService.evict(USERNAME)).thenReturn(Completable.complete());

        CredentialCreateCommand command = new CredentialCreateCommand(
                USERNAME, "Secret-123", CUSTOMER_ID, "john.doe@yanki.pe", List.of("ROLE_CUSTOMER"), null);

        StepVerifier.create(credentialService.create(command).toFlowable())
                .assertNext(credential -> {
                    assertThat(credential.getPasswordHash()).isEqualTo("hashed-password");
                    assertThat(credential.getUsername()).isEqualTo(USERNAME);
                    assertThat(credential.getCustomerId()).isEqualTo(CUSTOMER_ID);
                    assertThat(credential.getRoles()).containsExactly("ROLE_CUSTOMER");
                    assertThat(credential.isEnabled()).isTrue();
                    assertThat(credential.getCreatedAt()).isNotNull();
                })
                .verifyComplete();

        verify(credentialCacheService).evict(USERNAME);
    }

    @Test
    void shouldRejectUpdateWhenCredentialDoesNotExist() {
        when(credentialRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(credentialService.update("missing", new CredentialUpdateCommand(null, null, null, null, null)).toFlowable())
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) error).getCode()).isEqualTo("CREDENTIAL_NOT_FOUND");
                })
                .verify();
    }

    @Test
    void shouldKeepExistingHashWhenUpdateDoesNotIncludePassword() {
        Credential existing = credential();
        when(credentialRepository.findById("CREDENTIAL-1")).thenReturn(Mono.just(existing));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(credentialCacheService.evict(USERNAME)).thenReturn(Completable.complete());

        CredentialUpdateCommand command = new CredentialUpdateCommand(null, null, "nuevo@yanki.pe", List.of("ROLE_ADMIN"), false);

        StepVerifier.create(credentialService.update("CREDENTIAL-1", command).toFlowable())
                .assertNext(credential -> {
                    assertThat(credential.getPasswordHash()).isEqualTo("hashed-password");
                    assertThat(credential.getEmail()).isEqualTo("nuevo@yanki.pe");
                    assertThat(credential.getRoles()).containsExactly("ROLE_ADMIN");
                    assertThat(credential.isEnabled()).isFalse();
                })
                .verifyComplete();

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldDeleteCredentialAndInvalidateCache() {
        Credential existing = credential();
        when(credentialRepository.findById("CREDENTIAL-1")).thenReturn(Mono.just(existing));
        when(credentialRepository.delete(existing)).thenReturn(Mono.empty());
        when(credentialCacheService.evict(USERNAME)).thenReturn(Completable.complete());

        StepVerifier.create(credentialService.delete("CREDENTIAL-1").toFlowable())
                .expectComplete()
                .verify();

        verify(credentialRepository).delete(existing);
        verify(credentialCacheService).evict(USERNAME);
    }

    private Credential credential() {
        return Credential.builder()
                .id("CREDENTIAL-1")
                .username(USERNAME)
                .passwordHash("hashed-password")
                .customerId(CUSTOMER_ID)
                .email("john.doe@yanki.pe")
                .roles(List.of("ROLE_CUSTOMER"))
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
