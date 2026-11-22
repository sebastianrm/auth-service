package com.yanki.auth.infrastructure.cache;

import java.time.Duration;
import java.time.Instant;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Servicio de lista negra de tokens JWT sobre Redis.
 *
 * <p>Almacena los tokens revocados en Redis con un TTL igual al tiempo restante
 * de expiracion del token. Esto permite que el API Gateway consulte esta lista
 * antes de aceptar un token, logrando una revocacion efectiva sin romper el
 * modelo stateless de JWT.</p>
 *
 * <p>La clave de Redis sigue el patron {@code yanki:auth:blacklist:<token>}
 * para evitar colisiones con otras claves del sistema.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    /** Prefijo de las claves de lista negra de tokens en Redis. */
    private static final String BLACKLIST_KEY_PREFIX = "yanki:auth:blacklist:";

    /** Valor almacenado para marcar un token como revocado. */
    private static final String REVOKED_VALUE = "1";

    /** Plantilla reactiva de Redis con valores de tipo texto. */
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Agrega un token a la lista negra con el TTL correspondiente al tiempo
     * restante de expiracion.
     *
     * <p>Si el token ya expiro o el tiempo restante es cero o negativo,
     * no se almacena en Redis (no tiene sentido blacklisterizar un token
     * que ya es invalido por expiracion).</p>
     *
     * @param token el token JWT completo a revocar
     * @param expiresAt instante de expiracion del token
     * @return completable cuando la operacion termina
     */
    public Completable blacklistToken(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            log.warn("Token nulo o vacio recibido para blacklisterizar");
            return Completable.complete();
        }

        if (expiresAt == null) {
            log.warn("Fecha de expiracion nula para el token, no se puede calcular el TTL");
            return Completable.complete();
        }

        Instant now = Instant.now();
        Duration remaining = Duration.between(now, expiresAt);

        if (remaining.isNegative() || remaining.isZero()) {
            log.debug("Token ya expirado o sin tiempo restante, no se almacena en la lista negra");
            return Completable.complete();
        }

        String key = BLACKLIST_KEY_PREFIX + token;
        log.debug("Almacenando token en lista negra con TTL de {} segundos", remaining.getSeconds());

        return Completable.fromCompletionStage(
                redisTemplate.opsForValue().set(key, REVOKED_VALUE, remaining).toFuture()
        ).doOnComplete(() -> log.debug("Token agregado a la lista negra con TTL de {} segundos", remaining.getSeconds()))
         .doOnError(error -> log.error("Error al agregar token a la lista negra en Redis: {}", error.getMessage(), error));
    }
}
