package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.CredentialUpdateCommand;
import jakarta.validation.constraints.Email;
import java.util.List;

/**
 * Request de actualizacion de una credencial de acceso.
 *
 * <p>Todos los campos son opcionales: los valores {@code null} conservan el
 * dato almacenado actualmente.</p>
 *
 * @param password nueva contrasena del cliente
 * @param customerId identificador del cliente
 * @param email correo electronico del cliente
 * @param roles roles del cliente
 * @param enabled estado de la credencial
 */
public record CredentialUpdateRequest(
        String password,
        String customerId,
        @Email(message = "email no tiene un formato valido")
        String email,
        List<String> roles,
        Boolean enabled) {

    /**
     * Convierte el request en el comando de dominio.
     *
     * @return comando de actualizacion de credencial
     */
    public CredentialUpdateCommand toCommand() {
        return new CredentialUpdateCommand(password, customerId, email, roles, enabled);
    }
}
