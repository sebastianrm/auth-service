package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.IssuedTokens;
import java.util.List;

/**
 * Response con los tokens emitidos al autenticar a un cliente.
 *
 * @param accessToken access token JWT
 * @param tokenType tipo del token (siempre {@code Bearer})
 * @param expiresIn vigencia del access token en segundos
 * @param refreshToken refresh token JWT
 * @param username nombre de usuario del cliente
 * @param roles roles del cliente
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken, String username, List<String> roles) {

    /**
     * Construye el response a partir de los tokens emitidos.
     *
     * @param tokens tokens emitidos por el dominio
     * @return response de tokens
     */
    public static TokenResponse from(IssuedTokens tokens) {
        return new TokenResponse(
                tokens.accessToken(),
                tokens.tokenType(),
                tokens.expiresIn(),
                tokens.refreshToken(),
                tokens.username(),
                tokens.roles());
    }
}
