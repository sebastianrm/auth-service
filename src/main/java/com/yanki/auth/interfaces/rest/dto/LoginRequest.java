package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.LoginCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * Request de inicio de sesion.
 *
 * @param username nombre de usuario
 * @param password contrasena del cliente
 */
public record LoginRequest(
        @NotBlank(message = "username es obligatorio")
        String username,
        @NotBlank(message = "password es obligatorio")
        String password) {

    /**
     * Convierte el request en el comando de dominio.
     *
     * @return comando de inicio de sesion
     */
    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
