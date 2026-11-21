package com.yanki.auth.domain.service;

import com.yanki.auth.domain.event.CustomerRegisteredEvent;
import com.yanki.auth.domain.event.Topics;
import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.CredentialCreateCommand;
import com.yanki.auth.domain.model.CredentialUpdateCommand;
import com.yanki.auth.domain.model.ProcessedEvent;
import com.yanki.auth.domain.repository.CredentialRepository;
import com.yanki.auth.domain.repository.ProcessedEventRepository;
import com.yanki.auth.infrastructure.persistence.ReactiveAdapter;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de credenciales de acceso.
 *
 * <p>Gestiona el ciclo de vida de la coleccion {@code credentials}, mantiene
 * la cache de Redis sincronizada y crea credenciales a partir del evento
 * Kafka de registro de cliente de forma idempotente.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialService {

    /** Repositorio reactivo de credenciales. */
    private final CredentialRepository credentialRepository;

    /** Cache Redis de credenciales por username. */
    private final CredentialCacheService credentialCacheService;

    /** Codificador BCrypt de contrasenas. */
    private final PasswordEncoder passwordEncoder;

    /** Repositorio reactivo de marcas de idempotencia. */
    private final ProcessedEventRepository processedEventRepository;

    /**
     * Crea una credencial nueva con la contrasena hasheada.
     *
     * @param command datos de la credencial a crear
     * @return credencial creada
     */
    public Single<Credential> create(CredentialCreateCommand command) {
        Instant now = Instant.now();
        Credential credential = Credential.builder()
                .username(command.username())
                .passwordHash(passwordEncoder.encode(command.password()))
                .customerId(command.customerId())
                .email(command.email())
                .roles(command.roles() == null ? List.of() : List.copyOf(command.roles()))
                .enabled(command.enabled() == null || command.enabled())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return ReactiveAdapter.toSingle(credentialRepository.save(credential))
                .flatMap(saved -> credentialCacheService.evict(saved.getUsername()).andThen(Single.just(saved)))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof DuplicateKeyException) {
                        return Single.error(new BusinessException("CREDENTIAL_ALREADY_EXISTS",
                                "El usuario ya tiene una credencial registrada"));
                    }
                    return Single.error(ex);
                })
                .doOnSuccess(saved -> log.info("Credencial creada para el usuario {}", saved.getUsername()));
    }

    /**
     * Lista todas las credenciales registradas.
     *
     * @return flujo de credenciales
     */
    public Flowable<Credential> findAll() {
        return ReactiveAdapter.toFlowable(credentialRepository.findAll());
    }

    /**
     * Busca una credencial por su identificador.
     *
     * @param id identificador de la credencial
     * @return credencial encontrada o vacio
     */
    public Maybe<Credential> findById(String id) {
        return ReactiveAdapter.toMaybe(credentialRepository.findById(id));
    }

    /**
     * Actualiza una credencial existente.
     *
     * @param id identificador de la credencial
     * @param command datos a actualizar
     * @return credencial actualizada
     */
    public Single<Credential> update(String id, CredentialUpdateCommand command) {
        return ReactiveAdapter.toMaybe(credentialRepository.findById(id))
                .switchIfEmpty(Single.error(() -> new BusinessException("CREDENTIAL_NOT_FOUND", "Credencial no encontrada: " + id)))
                .map(current -> applyUpdate(current, command))
                .flatMap(current -> ReactiveAdapter.toSingle(credentialRepository.save(current)))
                .flatMap(saved -> credentialCacheService.evict(saved.getUsername()).andThen(Single.just(saved)))
                .doOnSuccess(saved -> log.info("Credencial {} actualizada", id));
    }

    /**
     * Elimina una credencial existente.
     *
     * @param id identificador de la credencial
     * @return completable cuando la credencial fue eliminada
     */
    public Completable delete(String id) {
        return ReactiveAdapter.toMaybe(credentialRepository.findById(id))
                .switchIfEmpty(Single.error(() -> new BusinessException("CREDENTIAL_NOT_FOUND", "Credencial no encontrada: " + id)))
                .flatMap(credential -> ReactiveAdapter.toCompletable(credentialRepository.delete(credential))
                        .andThen(credentialCacheService.evict(credential.getUsername()))
                        .andThen(Single.just(credential)))
                .doOnSuccess(credential -> log.info("Credencial {} eliminada", credential.getId()))
                .ignoreElement();
    }

    /**
     * Busca la credencial de un usuario sirviendo primero la cache Redis.
     *
     * @param username nombre de usuario
     * @return credencial encontrada o vacio
     */
    public Maybe<Credential> findByUsername(String username) {
        return credentialCacheService.get(username)
                .switchIfEmpty(Maybe.defer(() -> ReactiveAdapter.toMaybe(credentialRepository.findByUsername(username))
                        .flatMap(found -> credentialCacheService.put(found).andThen(Maybe.just(found)))));
    }

    /**
     * Invalida la entrada de cache de un usuario.
     *
     * <p>Se invoca durante el logout para forzar una recarga desde MongoDB
     * en la siguiente autenticacion.</p>
     *
     * @param username nombre de usuario cuya cache debe invalidarse
     * @return completable cuando la invalidacion termina
     */
    public Completable evictCache(String username) {
        return credentialCacheService.evict(username);
    }

    /**
     * Registra el inicio de sesion exitoso de una credencial.
     *
     * @param credential credencial autenticada
     * @return completable cuando el ultimo acceso quedo registrado
     */
    public Completable recordLogin(Credential credential) {
        Instant now = Instant.now();
        credential.setLastLoginAt(now);
        credential.setUpdatedAt(now);
        return ReactiveAdapter.toSingle(credentialRepository.save(credential))
                .flatMap(saved -> credentialCacheService.put(saved).andThen(Single.just(saved)))
                .doOnSuccess(saved -> log.info("Ultimo acceso del usuario {} actualizado", saved.getUsername()))
                .ignoreElement();
    }

    /**
     * Crea la credencial de un cliente a partir del evento de registro.
     *
     * <p>La operacion es idempotente: si el evento ya fue procesado se
     * descarta y si el usuario ya existe no se duplica la credencial.</p>
     *
     * @param event evento de registro de cliente
     * @return credencial creada o ya existente
     */
    public Single<Credential> registerFromEvent(CustomerRegisteredEvent event) {
        String eventKey = buildEventKey(event);
        return isProcessed(eventKey)
                .flatMap(processed -> {
                    if (Boolean.TRUE.equals(processed)) {
                        log.info("Evento {} ya procesado: se descarta", eventKey);
                        return findExistingCredential(event.username());
                    }
                    return createFromEvent(event, eventKey);
                });
    }

    /**
     * Indica si el evento ya fue procesado anteriormente.
     *
     * @param eventKey clave de idempotencia del evento
     * @return {@code true} si el evento ya fue procesado
     */
    private Single<Boolean> isProcessed(String eventKey) {
        return ReactiveAdapter.toSingle(processedEventRepository.existsByEventKey(eventKey).defaultIfEmpty(Boolean.FALSE))
                .onErrorReturnItem(Boolean.FALSE);
    }

    /**
     * Busca la credencial existente de un usuario.
     *
     * @param username nombre de usuario
     * @return credencial existente
     */
    private Single<Credential> findExistingCredential(String username) {
        return ReactiveAdapter.toMaybe(credentialRepository.findByUsername(username))
                .switchIfEmpty(Single.error(() -> new BusinessException("CREDENTIAL_NOT_FOUND",
                        "No existe credencial para el usuario " + username)));
    }

    /**
     * Crea la credencial del cliente registrado si todavia no existe.
     *
     * @param event evento de registro de cliente
     * @param eventKey clave de idempotencia del evento
     * @return credencial creada o ya existente
     */
    private Single<Credential> createFromEvent(CustomerRegisteredEvent event, String eventKey) {
        return ReactiveAdapter.toMaybe(credentialRepository.findByUsername(event.username()))
                .switchIfEmpty(Single.defer(() -> ReactiveAdapter.toSingle(credentialRepository.save(buildCredential(event)))))
                .flatMap(saved -> markProcessed(event, eventKey).andThen(Single.just(saved)))
                .flatMap(saved -> credentialCacheService.evict(saved.getUsername()).andThen(Single.just(saved)))
                .doOnSuccess(saved -> log.info("Credencial del cliente {} creada para el usuario {}",
                        event.customerId(), saved.getUsername()));
    }

    /**
     * Construye la credencial a persistir a partir del evento recibido.
     *
     * @param event evento de registro de cliente
     * @return credencial con la contrasena temporal hasheada
     */
    private Credential buildCredential(CustomerRegisteredEvent event) {
        Instant now = Instant.now();
        return Credential.builder()
                .username(event.username())
                .passwordHash(passwordEncoder.encode(event.temporaryPassword()))
                .customerId(event.customerId())
                .email(event.email())
                .roles(event.roles() == null ? List.of() : List.copyOf(event.roles()))
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Registra el evento como procesado para garantizar la idempotencia.
     *
     * @param event evento de registro de cliente
     * @param eventKey clave de idempotencia del evento
     * @return completable cuando el registro queda persistido
     */
    private Completable markProcessed(CustomerRegisteredEvent event, String eventKey) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventKey(eventKey)
                .topic(Topics.CUSTOMER_REGISTERED)
                .entityId(event.customerId())
                .processedAt(Instant.now())
                .build();
        return ReactiveAdapter.toSingle(processedEventRepository.save(processedEvent))
                .doOnSuccess(saved -> log.info("Evento {} registrado como procesado", eventKey))
                .onErrorResumeNext(ex -> {
                    log.warn("No se pudo registrar el evento {} como procesado: {}", eventKey, ex.getMessage());
                    return Single.just(processedEvent);
                })
                .ignoreElement();
    }

    /**
     * Construye la clave de idempotencia del evento de registro de cliente.
     *
     * @param event evento de registro de cliente
     * @return clave de idempotencia
     */
    private String buildEventKey(CustomerRegisteredEvent event) {
        return Topics.CUSTOMER_REGISTERED + ":" + event.customerId() + ":" + event.username();
    }

    /**
     * Aplica los datos de actualizacion sobre la credencial almacenada.
     *
     * @param current credencial almacenada
     * @param command datos a actualizar
     * @return credencial actualizada en memoria
     */
    private Credential applyUpdate(Credential current, CredentialUpdateCommand command) {
        if (command.password() != null && !command.password().isBlank()) {
            current.setPasswordHash(passwordEncoder.encode(command.password()));
        }
        if (command.customerId() != null) {
            current.setCustomerId(command.customerId());
        }
        if (command.email() != null) {
            current.setEmail(command.email());
        }
        if (command.roles() != null) {
            current.setRoles(List.copyOf(command.roles()));
        }
        if (command.enabled() != null) {
            current.setEnabled(command.enabled());
        }
        current.setUpdatedAt(Instant.now());
        return current;
    }
}
