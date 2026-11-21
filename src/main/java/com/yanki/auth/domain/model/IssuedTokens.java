package com.yanki.auth.domain.model;

import java.util.List;

/**
 * Tokens emitidos para una credencial autenticada.
 *
 * @param accessToken access token JWT
 * @param tokenType tipo del token (siempre {@code Bearer})
 * @param expiresIn vigencia del access token en segundos
 * @param refreshToken refresh token JWT
 * @param username nombre de usuario del cliente
 * @param roles roles del cliente
 */
public record IssuedTokens(String accessToken, String tokenType, long expiresIn, String refreshToken, String username, List<String> roles) {
}
