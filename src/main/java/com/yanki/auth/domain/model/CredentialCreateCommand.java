package com.yanki.auth.domain.model;

import java.util.List;

/**
 * Comando de creacion de una credencial de acceso.
 *
 * @param username nombre de usuario unico
 * @param password contrasena en claro (se hashea antes de persistir)
 * @param customerId identificador del cliente
 * @param email correo electronico del cliente
 * @param roles roles del cliente
 * @param enabled estado inicial de la credencial
 */
public record CredentialCreateCommand(String username, String password, String customerId, String email, List<String> roles, Boolean enabled) {
}
