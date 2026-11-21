package com.yanki.auth.interfaces.rest.dto;

/**
 * Response que confirma el cierre de sesion exitoso.
 *
 * @param message mensaje de confirmacion del logout
 * @param success indica si el logout se realizo correctamente
 */
public record LogoutResponse(String message, boolean success) {

    /**
     * Construye el response de logout exitoso.
     *
     * @return response con mensaje de confirmacion
     */
    public static LogoutResponse ok() {
        return new LogoutResponse("Sesion cerrada exitosamente", true);
    }
}
