package com.yanki.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.infrastructure.config.CacheProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Pruebas unitarias de la cache de credenciales sobre Redis.
 */
@ExtendWith(MockitoExtension.class)
class CredentialCacheServiceTest {

    private static final String USERNAME = "john.doe";
    private static final String KEY = "yanki:auth:credential:john.doe";
    private static final Duration TTL = Duration.ofMinutes(10);

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    private CredentialCacheService credentialCacheService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        CacheProperties cacheProperties = new CacheProperties();
        cacheProperties.setCredentialTtl(TTL);
        cacheProperties.setKeyPrefix("yanki:auth:credential:");
        credentialCacheService = new CredentialCacheService(redisTemplate, objectMapper, cacheProperties);
    }

    @Test
    void shouldReturnCredentialStoredInCache() throws Exception {
        Credential credential = credential();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(Mono.just(objectMapper.writeValueAsString(credential)));

        StepVerifier.create(credentialCacheService.get(USERNAME).toFlowable())
                .assertNext(cached -> {
                    assertThat(cached.getUsername()).isEqualTo(USERNAME);
                    assertThat(cached.getPasswordHash()).isEqualTo("hashed-password");
                    assertThat(cached.getRoles()).containsExactly("ROLE_CUSTOMER");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenCacheMisses() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(Mono.empty());

        StepVerifier.create(credentialCacheService.get(USERNAME).toFlowable())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldStoreCredentialWithConfiguredTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(eq(KEY), anyString(), eq(TTL))).thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(credentialCacheService.put(credential()).toFlowable())
                .expectNextCount(0)
                .verifyComplete();

        verify(valueOperations).set(eq(KEY), anyString(), eq(TTL));
    }

    @Test
    void shouldEvictCredentialFromCache() {
        when(redisTemplate.delete(KEY)).thenReturn(Mono.just(1L));

        StepVerifier.create(credentialCacheService.evict(USERNAME).toFlowable())
                .expectNextCount(0)
                .verifyComplete();

        verify(redisTemplate).delete(KEY);
    }

    @Test
    void shouldDegradeGracefullyWhenRedisFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(Mono.error(new IllegalStateException("redis no disponible")));

        StepVerifier.create(credentialCacheService.get(USERNAME).toFlowable())
                .expectNextCount(0)
                .verifyComplete();
    }

    private Credential credential() {
        return Credential.builder()
                .id("CREDENTIAL-1")
                .username(USERNAME)
                .passwordHash("hashed-password")
                .customerId("CUSTOMER-1")
                .email("john.doe@yanki.pe")
                .roles(List.of("ROLE_CUSTOMER"))
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
