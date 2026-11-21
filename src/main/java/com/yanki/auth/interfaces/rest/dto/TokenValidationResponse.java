package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.TokenValidationResult;
import java.util.List;

/**
 * Response con el resultado de validar un access token.
 *
 * @param valid indica si el token es valido
 * @param username nombre de usuario del sujeto del token
 * @param customerId identificador del cliente
 * @param roles roles del cliente
 */
public record TokenValidationResponse(boolean valid, String username, String customerId, List<String> roles) {

    /**
     * Construye el response a partir del resultado de validacion.
     *
     * @param result resultado de validacion del dominio
     * @return response de validacion
     */
    public static TokenValidationResponse from(TokenValidationResult result) {
        return new TokenValidationResponse(result.valid(), result.username(), result.customerId(), result.roles());
    }
}
