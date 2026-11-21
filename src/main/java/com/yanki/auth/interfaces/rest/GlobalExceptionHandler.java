package com.yanki.auth.interfaces.rest;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.interfaces.rest.dto.ApiError;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

/**
 * Manejador global de excepciones de la API.
 *
 * <p>Convierte cualquier error en el body estandar {@link ApiError} con el
 * codigo de negocio, el estado HTTP y la ruta que lo origino.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja los errores de negocio de la plataforma.
     *
     * @param ex error de negocio
     * @param exchange contexto de la peticion
     * @return respuesta con el detalle del error
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, ServerWebExchange exchange) {
        HttpStatus status = resolveStatus(ex.getCode());
        if (status.is5xxServerError()) {
            log.error("Error de negocio {}: {}", ex.getCode(), ex.getMessage());
        } else {
            log.warn("Error de negocio {}: {}", ex.getCode(), ex.getMessage());
        }
        return buildResponse(status, ex.getCode(), ex.getMessage(), exchange, null);
    }

    /**
     * Maneja los errores de validacion del cuerpo de la peticion.
     *
     * @param ex error de binding
     * @param exchange contexto de la peticion
     * @return respuesta con el detalle de los campos invalidos
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        Map<String, Object> details = new LinkedHashMap<>();
        ex.getFieldErrors().forEach(fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage()));
        ex.getGlobalErrors().forEach(globalError -> details.put(globalError.getObjectName(), globalError.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La peticion contiene datos invalidos", exchange, details);
    }

    /**
     * Maneja las peticiones mal formadas.
     *
     * @param ex error de entrada
     * @param exchange contexto de la peticion
     * @return respuesta con el detalle del error
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleInputException(ServerWebInputException ex, ServerWebExchange exchange) {
        log.warn("Peticion invalida en {}: {}", exchange.getRequest().getPath().value(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "La peticion no es valida", exchange, null);
    }

    /**
     * Maneja los conflictos de unicidad de la base de datos.
     *
     * @param ex error de clave duplicada
     * @param exchange contexto de la peticion
     * @return respuesta con el detalle del error
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKeyException(DuplicateKeyException ex, ServerWebExchange exchange) {
        log.warn("Recurso duplicado en {}: {}", exchange.getRequest().getPath().value(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "CREDENTIAL_ALREADY_EXISTS", "El recurso ya existe", exchange, null);
    }

    /**
     * Maneja las rutas no encontradas cuando ningun controlador coincide con la peticion.
     *
     * <p>Spring WebFlux delega al {@code ResourceWebHandler} cuando no hay ruta
     * registrada para el path solicitado, lanzando {@link NoResourceFoundException}.
     * Se captura aqui para devolver un 404 apropiado en lugar de que caiga en el
     * manejador generico de excepciones inesperadas.</p>
     *
     * @param ex excepcion de recurso no encontrado
     * @param exchange contexto de la peticion
     * @return respuesta 404 con el detalle del error
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFoundException(NoResourceFoundException ex, ServerWebExchange exchange) {
        log.warn("Ruta no encontrada: {}", exchange.getRequest().getPath().value());
        return buildResponse(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND",
                "La ruta solicitada no existe: " + exchange.getRequest().getPath().value(), exchange, null);
    }

    /**
     * Maneja cualquier error no controlado.
     *
     * @param ex error inesperado
     * @param exchange contexto de la peticion
     * @return respuesta con el detalle del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception ex, ServerWebExchange exchange) {
        log.error("Error inesperado en {}: {}", exchange.getRequest().getPath().value(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrio un error inesperado", exchange, null);
    }

    /**
     * Construye la respuesta de error estandar.
     *
     * @param status estado HTTP de la respuesta
     * @param code codigo de negocio del error
     * @param message descripcion legible del error
     * @param exchange contexto de la peticion
     * @param details detalle adicional del error
     * @return respuesta con el body {@link ApiError}
     */
    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message,
            ServerWebExchange exchange, Map<String, Object> details) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                exchange.getRequest().getPath().value(),
                details);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Traduce el codigo de negocio al estado HTTP correspondiente.
     *
     * @param code codigo de negocio del error
     * @return estado HTTP de la respuesta
     */
    private HttpStatus resolveStatus(String code) {
        return switch (code) {
            case "INVALID_CREDENTIALS", "INVALID_TOKEN", "TOKEN_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            case "CREDENTIAL_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CREDENTIAL_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
