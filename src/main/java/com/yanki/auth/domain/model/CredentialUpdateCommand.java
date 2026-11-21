package com.yanki.auth.domain.model;

import java.util.List;

/**
 * Comando de actualizacion de una credencial de acceso.
 *
 * <p>Todos los campos son opcionales: los valores {@code null} conservan el
 * dato almacenado actualmente.</p>
 *
 * @param password nueva contrasena en claro (se hashea antes de persistir)
 * @param customerId identificador del cliente
 * @param email correo electronico del cliente
 * @param roles roles del cliente
 * @param enabled estado de la credencial
 */
public record CredentialUpdateCommand(String password, String customerId, String email, List<String> roles, Boolean enabled) {
}
