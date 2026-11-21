package com.yanki.auth.interfaces.rest.controller;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.service.CredentialService;
import com.yanki.auth.interfaces.rest.dto.CredentialCreateRequest;
import com.yanki.auth.interfaces.rest.dto.CredentialResponse;
import com.yanki.auth.interfaces.rest.dto.CredentialUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST de credenciales de acceso.
 *
 * <p>Expone el CRUD de la coleccion {@code credentials}. Las contrasenas se
 * reciben en claro por el canal seguro de la API pero nunca se devuelven:
 * solo se persiste su hash BCrypt.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/credentials")
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "Gestion de credenciales de acceso de los clientes")
public class CredentialController {

    /** Servicio de credenciales de acceso. */
    private final CredentialService credentialService;

    /**
     * Crea una credencial de acceso.
     *
     * @param request datos de la credencial a crear
     * @return credencial creada
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una credencial de acceso", description = "Registra una nueva credencial hasheando la contrasena con BCrypt")
    public Mono<CredentialResponse> create(@Valid @RequestBody CredentialCreateRequest request) {
        return Mono.from(credentialService.create(request.toCommand()).toFlowable())
                .map(CredentialResponse::from);
    }

    /**
     * Lista todas las credenciales registradas.
     *
     * @return flujo de credenciales
     */
    @GetMapping
    @Operation(summary = "Lista las credenciales de acceso", description = "Devuelve todas las credenciales registradas")
    public Flux<CredentialResponse> findAll() {
        return Flux.from(credentialService.findAll())
                .map(CredentialResponse::from);
    }

    /**
     * Consulta una credencial por su identificador.
     *
     * @param id identificador de la credencial
     * @return credencial encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consulta una credencial por su identificador")
    public Mono<CredentialResponse> findById(@PathVariable String id) {
        return Mono.from(credentialService.findById(id).toFlowable())
                .switchIfEmpty(Mono.error(() -> new BusinessException("CREDENTIAL_NOT_FOUND", "Credencial no encontrada: " + id)))
                .map(CredentialResponse::from);
    }

    /**
     * Actualiza una credencial existente.
     *
     * @param id identificador de la credencial
     * @param request datos a actualizar
     * @return credencial actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una credencial de acceso", description = "Actualiza los datos de la credencial indicada")
    public Mono<CredentialResponse> update(@PathVariable String id, @Valid @RequestBody CredentialUpdateRequest request) {
        return Mono.from(credentialService.update(id, request.toCommand()).toFlowable())
                .map(CredentialResponse::from);
    }

    /**
     * Elimina una credencial existente.
     *
     * @param id identificador de la credencial
     * @return completable cuando la credencial fue eliminada
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una credencial de acceso")
    public Mono<Void> delete(@PathVariable String id) {
        return Mono.from(credentialService.delete(id).toFlowable())
                .then();
    }
}
