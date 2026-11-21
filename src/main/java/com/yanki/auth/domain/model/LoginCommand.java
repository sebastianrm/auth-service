package com.yanki.auth.domain.model;

/**
 * Comando de inicio de sesion.
 *
 * @param username nombre de usuario
 * @param password contrasena en claro
 */
public record LoginCommand(String username, String password) {
}
