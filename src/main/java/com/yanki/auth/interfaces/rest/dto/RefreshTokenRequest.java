package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.RefreshCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * Request de refresco de token.
 *
 * @param refreshToken refresh token previamente emitido
 */
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken es obligatorio")
        String refreshToken) {

    /**
     * Convierte el request en el comando de dominio.
     *
     * @return comando de refresco de token
     */
    public RefreshCommand toCommand() {
        return new RefreshCommand(refreshToken);
    }
}
