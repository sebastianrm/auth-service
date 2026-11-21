package com.yanki.auth.domain.model;

/**
 * Comando de refresco de token.
 *
 * @param refreshToken refresh token previamente emitido
 */
public record RefreshCommand(String refreshToken) {
}
