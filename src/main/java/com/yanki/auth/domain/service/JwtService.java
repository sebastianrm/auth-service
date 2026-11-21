package com.yanki.auth.domain.service;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.TokenClaims;
import com.yanki.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de emision y validacion de tokens JWT (algoritmo HS256).
 *
 * <p>Los tokens emitidos incluyen los claims {@code sub} (username),
 * {@code customerId}, {@code roles}, {@code type} e {@code iss}. El access
 * token usa el claim {@code type=access} y el refresh token
 * {@code type=refresh} con una vigencia mayor.</p>
 */
@Slf4j
@Service
public class JwtService {

    /** Claim con el identificador del cliente. */
    public static final String CLAIM_CUSTOMER_ID = "customerId";

    /** Claim con la lista de roles del cliente. */
    public static final String CLAIM_ROLES = "roles";

    /** Claim con el tipo de token. */
    public static final String CLAIM_TYPE = "type";

    /** Tipo de token de acceso. */
    public static final String TYPE_ACCESS = "access";

    /** Tipo de token de refresco. */
    public static final String TYPE_REFRESH = "refresh";

    /** Longitud minima del secreto en bytes para firmar con HS256. */
    private static final int MINIMUM_SECRET_LENGTH = 32;

    /** Configuracion de los tokens JWT. */
    private final JwtProperties jwtProperties;

    /** Clave de firma HS256 derivada del secreto configurado. */
    private final SecretKey signingKey;

    /**
     * Crea el servicio de tokens JWT.
     *
     * @param jwtProperties configuracion externalizada de los tokens
     */
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = buildSigningKey(jwtProperties.getSecret());
    }

    /**
     * Emite un access token para la credencial indicada.
     *
     * @param credential credencial del cliente
     * @return token JWT firmado
     */
    public String generateAccessToken(Credential credential) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(jwtProperties.getAccessTokenExpiration());
        log.debug("Emitiendo access token para {}", credential.getUsername());
        return Jwts.builder()
                .subject(credential.getUsername())
                .claim(CLAIM_CUSTOMER_ID, credential.getCustomerId())
                .claim(CLAIM_ROLES, credential.getRoles())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Emite un refresh token para la credencial indicada.
     *
     * @param credential credencial del cliente
     * @return token JWT firmado con claim {@code type=refresh}
     */
    public String generateRefreshToken(Credential credential) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(jwtProperties.getRefreshTokenExpiration());
        log.debug("Emitiendo refresh token para {}", credential.getUsername());
        return Jwts.builder()
                .subject(credential.getUsername())
                .claim(CLAIM_CUSTOMER_ID, credential.getCustomerId())
                .claim(CLAIM_ROLES, credential.getRoles())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valida un token JWT y devuelve sus claims.
     *
     * @param token token JWT recibido
     * @param expectedType tipo esperado del token ({@code access} o {@code refresh})
     * @return claims validados del token
     * @throws BusinessException si el token es invalido, ha expirado o no es del tipo esperado
     */
    public TokenClaims parseToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String type = claims.get(CLAIM_TYPE, String.class);
            if (expectedType != null && !expectedType.equals(type)) {
                throw new BusinessException("INVALID_TOKEN", "El token no es del tipo esperado: " + expectedType);
            }
            return new TokenClaims(
                    claims.getSubject(),
                    claims.get(CLAIM_CUSTOMER_ID, String.class),
                    extractRoles(claims),
                    type,
                    toInstant(claims.getIssuedAt()),
                    toInstant(claims.getExpiration()));
        } catch (ExpiredJwtException ex) {
            throw new BusinessException("TOKEN_EXPIRED", "El token ha expirado");
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token rechazado: {}", ex.getMessage());
            throw new BusinessException("INVALID_TOKEN", "El token es invalido");
        }
    }

    /**
     * Devuelve la vigencia del access token expresada en segundos.
     *
     * @return numero de segundos de vigencia
     */
    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration().toSeconds();
    }

    /**
     * Devuelve la vigencia del refresh token expresada en segundos.
     *
     * @return numero de segundos de vigencia
     */
    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpiration().toSeconds();
    }

    /**
     * Extrae la lista de roles del claim {@code roles}.
     *
     * @param claims claims del token
     * @return lista de roles, vacia si el claim no existe
     */
    private List<String> extractRoles(Claims claims) {
        Object roles = claims.get(CLAIM_ROLES);
        if (roles instanceof List<?> roleList) {
            return roleList.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    /**
     * Convierte una fecha de JJWT a instante.
     *
     * @param date fecha de JJWT
     * @return instante equivalente o {@code null}
     */
    private Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    /**
     * Deriva la clave de firma HS256 a partir del secreto configurado.
     *
     * @param secret secreto configurado
     * @return clave de firma
     */
    private static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La propiedad yanki.security.jwt.secret es obligatoria");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException("La propiedad yanki.security.jwt.secret debe tener al menos 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
