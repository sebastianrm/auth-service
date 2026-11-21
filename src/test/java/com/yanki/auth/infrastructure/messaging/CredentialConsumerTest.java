package com.yanki.auth.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yanki.auth.domain.event.CustomerRegisteredEvent;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.ProcessedEvent;
import com.yanki.auth.domain.repository.CredentialRepository;
import com.yanki.auth.domain.repository.ProcessedEventRepository;
import com.yanki.auth.domain.service.CredentialCacheService;
import com.yanki.auth.domain.service.CredentialService;
import io.reactivex.rxjava3.core.Completable;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Pruebas unitarias del consumidor Kafka de registro de cliente.
 */
@ExtendWith(MockitoExtension.class)
class CredentialConsumerTest {

    private static final String CUSTOMER_ID = "CUSTOMER-1";
    private static final String USERNAME = "john.doe";
    private static final String EMAIL = "john.doe@yanki.pe";
    private static final String TEMPORARY_PASSWORD = "Temp-1234";
    private static final String PASSWORD_HASH = "$2a$10$temporaryHash";

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private CredentialCacheService credentialCacheService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CredentialConsumer credentialConsumer;

    @BeforeEach
    void setUp() {
        CredentialService credentialService = new CredentialService(
                credentialRepository, credentialCacheService, passwordEncoder, processedEventRepository);
        credentialConsumer = new CredentialConsumer(credentialService);
    }

    @Test
    void shouldCreateCredentialWhenCustomerRegisteredEventIsConsumed() {
        when(processedEventRepository.existsByEventKey(anyString())).thenReturn(Mono.just(false));
        when(credentialRepository.findByUsername(USERNAME)).thenReturn(Mono.empty());
        when(passwordEncoder.encode(TEMPORARY_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(credentialCacheService.evict(USERNAME)).thenReturn(Completable.complete());

        StepVerifier.create(credentialConsumer.processRegistration(event()).toFlowable())
                .expectComplete()
                .verify();

        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepository).save(credentialCaptor.capture());
        Credential saved = credentialCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo(USERNAME);
        assertThat(saved.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(saved.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getRoles()).containsExactly("ROLE_CUSTOMER");
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void shouldNotDuplicateCredentialWhenEventIsRepeated() {
        when(processedEventRepository.existsByEventKey(anyString())).thenReturn(Mono.just(true));
        when(credentialRepository.findByUsername(USERNAME)).thenReturn(Mono.just(existingCredential()));

        StepVerifier.create(credentialConsumer.processRegistration(event()).toFlowable())
                .expectComplete()
                .verify();

        verify(credentialRepository, never()).save(any(Credential.class));
    }

    @Test
    void shouldNotDuplicateCredentialWhenUsernameAlreadyExists() {
        when(processedEventRepository.existsByEventKey(anyString())).thenReturn(Mono.just(false));
        when(credentialRepository.findByUsername(USERNAME)).thenReturn(Mono.just(existingCredential()));
        when(processedEventRepository.save(any(ProcessedEvent.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(credentialCacheService.evict(USERNAME)).thenReturn(Completable.complete());

        StepVerifier.create(credentialConsumer.processRegistration(event()).toFlowable())
                .expectComplete()
                .verify();

        verify(credentialRepository, never()).save(any(Credential.class));
    }

    private CustomerRegisteredEvent event() {
        return new CustomerRegisteredEvent(CUSTOMER_ID, USERNAME, EMAIL, TEMPORARY_PASSWORD, List.of("ROLE_CUSTOMER"), "PERSONAL", "ACTIVE");
    }

    private Credential existingCredential() {
        return Credential.builder()
                .id("CREDENTIAL-1")
                .username(USERNAME)
                .passwordHash(PASSWORD_HASH)
                .customerId(CUSTOMER_ID)
                .email(EMAIL)
                .roles(List.of("ROLE_CUSTOMER"))
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
