package com.yanki.auth.interfaces.rest.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Body estandar de error de la API.
 *
 * @param timestamp instante de generacion del error
 * @param status codigo de estado HTTP
 * @param error descripcion textual del estado HTTP
 * @param code codigo de negocio del error
 * @param message descripcion legible del error
 * @param path ruta que originó el error
 * @param details detalle adicional del error (puede ser nulo)
 */
public record ApiError(Instant timestamp, int status, String error, String code, String message, String path, Map<String, Object> details) {
}
