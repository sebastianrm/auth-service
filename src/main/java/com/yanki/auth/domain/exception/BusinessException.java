package com.yanki.auth.domain.exception;

/**
 * Error de negocio de la plataforma Yanki.
 *
 * <p>El {@code code} identifica la regla violada y se expone en el body de
 * error {@code ApiError} de la API.</p>
 */
public class BusinessException extends RuntimeException {

    /** Codigo de negocio del error. */
    private final String code;

    /**
     * Crea un error de negocio.
     *
     * @param code codigo de negocio del error
     * @param message descripcion legible del error
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Devuelve el codigo de negocio del error.
     *
     * @return codigo de negocio
     */
    public String getCode() {
        return code;
    }
}
