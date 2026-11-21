package com.yanki.auth.infrastructure.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuracion de los tokens JWT.
 *
 * <p>Se externalizan desde {@code application.yml} con las variables de
 * entorno {@code JWT_SECRET}, {@code JWT_ISSUER},
 * {@code JWT_ACCESS_TOKEN_EXPIRATION} y {@code JWT_REFRESH_TOKEN_EXPIRATION}.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yanki.security.jwt")
public class JwtProperties {

    /** Secreto de firma HS256; debe tener al menos 32 bytes. */
    private String secret;

    /** Emisor (claim {@code iss}) de los tokens emitidos. */
    private String issuer = "yanki-auth-service";

    /** Vigencia del access token. */
    private Duration accessTokenExpiration = Duration.ofHours(1);

    /** Vigencia del refresh token. */
    private Duration refreshTokenExpiration = Duration.ofDays(7);
}
