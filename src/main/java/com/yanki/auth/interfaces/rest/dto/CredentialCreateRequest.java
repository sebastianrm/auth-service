package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.CredentialCreateCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request de creacion de una credencial de acceso.
 *
 * @param username nombre de usuario unico
 * @param password contrasena del cliente
 * @param customerId identificador del cliente
 * @param email correo electronico del cliente
 * @param roles roles del cliente
 * @param enabled estado inicial de la credencial
 */
public record CredentialCreateRequest(
        @NotBlank(message = "username es obligatorio")
        String username,
        @NotBlank(message = "password es obligatorio")
        String password,
        String customerId,
        @Email(message = "email no tiene un formato valido")
        String email,
        List<String> roles,
        Boolean enabled) {

    /**
     * Convierte el request en el comando de dominio.
     *
     * @return comando de creacion de credencial
     */
    public CredentialCreateCommand toCommand() {
        return new CredentialCreateCommand(username, password, customerId, email, roles, enabled);
    }
}
