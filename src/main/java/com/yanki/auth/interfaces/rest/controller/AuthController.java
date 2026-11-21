package com.yanki.auth.interfaces.rest.controller;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.service.AuthService;
import com.yanki.auth.interfaces.rest.dto.LoginRequest;
import com.yanki.auth.interfaces.rest.dto.LogoutResponse;
import com.yanki.auth.interfaces.rest.dto.RefreshTokenRequest;
import com.yanki.auth.interfaces.rest.dto.TokenResponse;
import com.yanki.auth.interfaces.rest.dto.TokenValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador REST de autenticacion.
 *
 * <p>Expone el login, el refresco de tokens y la validacion del access token
 * Bearer. Las contrasenas se verifican con BCrypt y los tokens se firman
 * con JWT HS256.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacion de clientes y emision de tokens JWT")
public class AuthController {

    /** Prefijo del encabezado Authorization con el esquema Bearer. */
    private static final String BEARER_PREFIX = "Bearer ";

    /** Servicio de autenticacion. */
    private final AuthService authService;

    /**
     * Autentica a un cliente y emite los tokens de acceso y refresco.
     *
     * @param request credenciales del cliente
     * @return tokens emitidos
     */
    @PostMapping("/login")
    @Operation(summary = "Autentica a un cliente", description = "Verifica las credenciales y emite un access token y un refresh token JWT")
    public Mono<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Mono.from(authService.login(request.toCommand()).toFlowable())
                .map(TokenResponse::from);
    }

    /**
     * Emite un nuevo access token a partir de un refresh token valido.
     *
     * @param request refresh token del cliente
     * @return tokens emitidos
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresca el access token", description = "Emite un nuevo access token usando el refresh token recibido")
    public Mono<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Mono.from(authService.refresh(request.toCommand()).toFlowable())
                .map(TokenResponse::from);
    }

    /**
     * Invalida la sesion del usuario eliminando su entrada de cache en Redis.
     *
     * <p>El cliente debe enviar el access token en el encabezado Authorization
     * con el esquema Bearer. Como los tokens JWT son stateless la revocacion
     * real queda en manos del cliente (descartando el token), pero el servidor
     * limpia la cache de credenciales para forzar una recarga en la siguiente
     * operacion.</p>
     *
     * @param authorization encabezado Authorization con el token Bearer
     * @return 200 OK con mensaje de confirmacion cuando el logout se completa
     */
    @PostMapping("/logout")
    @Operation(summary = "Cierra la sesion del usuario", description = "Invalida la cache de credenciales del usuario autenticado y senaliza el fin de sesion")
    public Mono<ResponseEntity<LogoutResponse>> logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return Mono.fromCallable(() -> extractBearerToken(authorization))
                .flatMap(token -> Mono.from(authService.logout(token).toFlowable()))
                .then(Mono.just(ResponseEntity.ok(LogoutResponse.ok())));
    }

    /**
     * Valida el access token Bearer recibido.
     *
     * @param authorization encabezado Authorization con el token Bearer
     * @return resultado de la validacion del token
     */
    @GetMapping("/validate")
    @Operation(summary = "Valida el access token", description = "Valida el token Bearer recibido y devuelve sus claims")
    public Mono<TokenValidationResponse> validate(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return Mono.fromCallable(() -> extractBearerToken(authorization))
                .flatMap(token -> Mono.from(authService.validate(token).toFlowable()))
                .map(TokenValidationResponse::from);
    }

    /**
     * Extrae el token del encabezado Authorization.
     *
     * @param authorization encabezado Authorization recibido
     * @return token sin el esquema Bearer
     */
    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("INVALID_TOKEN", "El encabezado Authorization debe tener el formato Bearer <token>");
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
