package com.yanki.auth.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.infrastructure.config.CacheProperties;
import com.yanki.auth.infrastructure.persistence.ReactiveAdapter;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Cache de credenciales sobre Redis.
 *
 * <p>Las credenciales se almacenan serializadas como JSON bajo la clave
 * {@code yanki:auth:credential:<username>} con un TTL configurable. Los
 * fallos de Redis no rompen el flujo de autenticacion: se degradan a cache
 * miss y se registran en el log.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CredentialCacheService {

    /** Plantilla reactiva de Redis con valores de tipo texto. */
    private final ReactiveStringRedisTemplate redisTemplate;

    /** Serializador JSON usado para guardar las credenciales. */
    private final ObjectMapper objectMapper;

    /** Configuracion de TTL y prefijo de claves. */
    private final CacheProperties cacheProperties;

    /**
     * Obtiene la credencial cacheada de un usuario.
     *
     * @param username nombre de usuario
     * @return credencial cacheada o vacio si no existe o falla Redis
     */
    public Maybe<Credential> get(String username) {
        Mono<Credential> cached = redisTemplate.opsForValue()
                .get(buildKey(username))
                .flatMap(this::deserialize)
                .onErrorResume(ex -> {
                    log.warn("No se pudo leer la cache de credenciales: {}", ex.getMessage());
                    return Mono.empty();
                });
        return ReactiveAdapter.toMaybe(cached);
    }

    /**
     * Guarda la credencial en cache con el TTL configurado.
     *
     * @param credential credencial a cachear
     * @return completable cuando la escritura termina
     */
    public Completable put(Credential credential) {
        Mono<Boolean> stored = Mono.fromCallable(() -> objectMapper.writeValueAsString(credential))
                .flatMap(json -> redisTemplate.opsForValue()
                        .set(buildKey(credential.getUsername()), json, cacheProperties.getCredentialTtl()))
                .doOnSuccess(saved -> log.debug("Credencial {} cacheada", credential.getUsername()))
                .onErrorResume(ex -> {
                    log.warn("No se pudo cachear la credencial {}: {}", credential.getUsername(), ex.getMessage());
                    return Mono.empty();
                });
        return ReactiveAdapter.toCompletable(stored);
    }

    /**
     * Elimina del cache la credencial de un usuario.
     *
     * @param username nombre de usuario
     * @return completable cuando la invalidacion termina
     */
    public Completable evict(String username) {
        Mono<Long> removed = redisTemplate.delete(buildKey(username))
                .doOnSuccess(count -> log.debug("Credencial {} invalidada del cache", username))
                .onErrorResume(ex -> {
                    log.warn("No se pudo invalidar la cache de {}: {}", username, ex.getMessage());
                    return Mono.empty();
                });
        return ReactiveAdapter.toCompletable(removed);
    }

    /**
     * Construye la clave de cache de un usuario.
     *
     * @param username nombre de usuario
     * @return clave de cache
     */
    private String buildKey(String username) {
        return cacheProperties.getKeyPrefix() + username;
    }

    /**
     * Deserializa la credencial almacenada en cache.
     *
     * @param json contenido JSON cacheado
     * @return credencial deserializada
     */
    private Mono<Credential> deserialize(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, Credential.class));
    }
}
