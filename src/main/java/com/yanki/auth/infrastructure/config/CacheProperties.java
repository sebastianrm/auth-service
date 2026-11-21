package com.yanki.auth.infrastructure.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuracion de la cache Redis del servicio.
 *
 * <p>Se externalizan desde {@code application.yml} con las variables de
 * entorno {@code CREDENTIAL_CACHE_TTL} y {@code CREDENTIAL_CACHE_KEY_PREFIX}.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yanki.cache")
public class CacheProperties {

    /** Tiempo de vida de la credencial cacheada en Redis. */
    private Duration credentialTtl = Duration.ofMinutes(15);

    /** Prefijo de las claves de cache del dominio de credenciales. */
    private String keyPrefix = "yanki:auth:credential:";
}
