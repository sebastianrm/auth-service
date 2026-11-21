package com.yanki.auth.domain.service;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.IssuedTokens;
import com.yanki.auth.domain.model.LoginCommand;
import com.yanki.auth.domain.model.RefreshCommand;
import com.yanki.auth.domain.model.TokenClaims;
import com.yanki.auth.domain.model.TokenValidationResult;
import com.yanki.auth.infrastructure.cache.TokenBlacklistService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticacion de clientes.
 *
 * <p>Verifica las credenciales con BCrypt, emite access y refresh tokens
 * JWT y valida los tokens Bearer recibidos.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** Tipo de token devuelto en las respuestas de autenticacion. */
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    /** Servicio de credenciales de acceso. */
    private final CredentialService credentialService;

    /** Servicio de emision y validacion de tokens JWT. */
    private final JwtService jwtService;

    /** Codificador BCrypt de contrasenas. */
    private final PasswordEncoder passwordEncoder;

    /** Servicio de lista negra de tokens en Redis. */
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Autentica a un cliente y emite los tokens de acceso y refresco.
     *
     * @param command credenciales del cliente
     * @return tokens emitidos con los datos del usuario
     */
    public Single<IssuedTokens> login(LoginCommand command) {
        return credentialService.findByUsername(command.username())
                .switchIfEmpty(Single.error(() -> invalidCredentials()))
                .map(credential -> {
                    if (!credential.isEnabled() || !passwordEncoder.matches(command.password(), credential.getPasswordHash())) {
                        throw invalidCredentials();
                    }
                    return credential;
                })
                .flatMap(credential -> credentialService.recordLogin(credential).andThen(Single.just(credential)))
                .map(this::buildIssuedTokens)
                .doOnSuccess(tokens -> log.info("Login exitoso del usuario {}", command.username()));
    }

    /**
     * Emite un nuevo access token a partir de un refresh token valido.
     *
     * @param command refresh token del cliente
     * @return tokens emitidos con los datos del usuario
     */
    public Single<IssuedTokens> refresh(RefreshCommand command) {
        return Single.defer(() -> {
            TokenClaims claims = jwtService.parseToken(command.refreshToken(), JwtService.TYPE_REFRESH);
            return credentialService.findByUsername(claims.username())
                    .switchIfEmpty(Single.error(() -> new BusinessException("INVALID_CREDENTIALS",
                            "El usuario del refresh token ya no existe")))
                    .map(credential -> {
                        if (!credential.isEnabled()) {
                            throw new BusinessException("INVALID_CREDENTIALS", "La credencial del usuario esta deshabilitada");
                        }
                        return credential;
                    })
                    .flatMap(credential -> credentialService.recordLogin(credential).andThen(Single.just(credential)))
                    .map(this::buildIssuedTokens);
        });
    }

    /**
     * Invalida la sesion del usuario agregando el token a la lista negra en Redis
     * y eliminando su entrada de cache de credenciales.
     *
     * <p>El token se almacena en la lista negra con un TTL igual al tiempo restante
     * de expiracion, de modo que el API Gateway puede rechazarlo inmediatamente.
     * Ademas, se limpia la cache de credenciales para forzar una recarga desde
     * MongoDB en la siguiente operacion, lo que permite detectar cambios de estado
     * (usuario deshabilitado, contrasena modificada, etc.).</p>
     *
     * @param accessToken token de acceso Bearer del usuario que hace logout
     * @return completable cuando la cache ha sido invalidada y el token blacklisterizado
     */
    public Completable logout(String accessToken) {
        log.info("Procesando logout con el access token recibido");
        return Single.defer(() -> {
            try {
                log.debug("Parseando el access token para obtener los claims");
                TokenClaims claims = jwtService.parseToken(accessToken, JwtService.TYPE_ACCESS);
                log.info("Token parseado exitosamente para el usuario: {}", claims.username());
                return Single.just(claims);
            } catch (Exception ex) {
                log.error("Error al parsear el token en el logout: {}", ex.getMessage(), ex);
                throw ex;
            }
        })
                .flatMapCompletable(claims -> {
                    log.info("Iniciando proceso de revocacion para el usuario: {}", claims.username());
                    // Paso 1: Agregar el token a la lista negra en Redis
                    return tokenBlacklistService.blacklistToken(accessToken, claims.expiresAt())
                            .andThen(
                                    // Paso 2: Eliminar la credencial de la cache
                                    credentialService.evictCache(claims.username())
                                            .doOnComplete(() -> {
                                                log.info("Logout exitoso del usuario {}: token blacklisterizado y cache evictada",
                                                        claims.username());
                                            })
                                            .doOnError(error -> {
                                                log.error("Error al evictar la cache del usuario {}: {}",
                                                        claims.username(), error.getMessage(), error);
                                            })
                            );
                })
                .doOnError(error -> {
                    log.error("Error durante el proceso de logout: {}", error.getMessage(), error);
                });
    }

    /**
     * Valida un access token Bearer y devuelve sus claims.
     *
     * @param token access token recibido
     * @return resultado de la validacion del token
     */
    public Single<TokenValidationResult> validate(String token) {
        return Single.defer(() -> {
            TokenClaims claims = jwtService.parseToken(token, JwtService.TYPE_ACCESS);
            return Single.just(new TokenValidationResult(true, claims.username(), claims.customerId(), claims.roles()));
        });
    }

    /**
     * Construye los tokens emitidos para una credencial.
     *
     * @param credential credencial autenticada
     * @return tokens emitidos
     */
    private IssuedTokens buildIssuedTokens(Credential credential) {
        return new IssuedTokens(
                jwtService.generateAccessToken(credential),
                TOKEN_TYPE_BEARER,
                jwtService.getAccessTokenExpirationSeconds(),
                jwtService.generateRefreshToken(credential),
                credential.getUsername(),
                credential.getRoles());
    }

    /**
     * Crea el error de negocio para credenciales invalidas.
     *
     * @return error de credenciales invalidas
     */
    private BusinessException invalidCredentials() {
        return new BusinessException("INVALID_CREDENTIALS", "Las credenciales proporcionadas no son validas");
    }
}
