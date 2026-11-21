package com.yanki.auth.interfaces.rest.dto;

import com.yanki.auth.domain.model.Credential;
import java.time.Instant;
import java.util.List;

/**
 * Response de una credencial de acceso.
 *
 * <p>Nunca expone el hash de la contrasena.</p>
 *
 * @param id identificador de la credencial
 * @param username nombre de usuario
 * @param customerId identificador del cliente
 * @param email correo electronico del cliente
 * @param roles roles del cliente
 * @param enabled estado de la credencial
 * @param createdAt instante de creacion
 * @param updatedAt instante de ultima actualizacion
 * @param lastLoginAt instante del ultimo inicio de sesion
 */
public record CredentialResponse(
        String id,
        String username,
        String customerId,
        String email,
        List<String> roles,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt) {

    /**
     * Construye el response a partir de la entidad de dominio.
     *
     * @param credential credencial de dominio
     * @return response de credencial
     */
    public static CredentialResponse from(Credential credential) {
        return new CredentialResponse(
                credential.getId(),
                credential.getUsername(),
                credential.getCustomerId(),
                credential.getEmail(),
                credential.getRoles(),
                credential.isEnabled(),
                credential.getCreatedAt(),
                credential.getUpdatedAt(),
                credential.getLastLoginAt());
    }
}
