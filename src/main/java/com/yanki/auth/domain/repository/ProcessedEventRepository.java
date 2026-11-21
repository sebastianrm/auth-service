package com.yanki.auth.domain.repository;

import com.yanki.auth.domain.model.ProcessedEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo de marcas de idempotencia de eventos consumidos.
 *
 * <p>Solo se usan metodos derivados de Spring Data (sin {@code @Query}).</p>
 */
public interface ProcessedEventRepository extends ReactiveMongoRepository<ProcessedEvent, String> {

    /**
     * Busca el registro de idempotencia de una clave de evento.
     *
     * @param eventKey clave unica de idempotencia
     * @return registro encontrado o vacio
     */
    Mono<ProcessedEvent> findByEventKey(String eventKey);

    /**
     * Indica si ya se proceso el evento identificado por la clave.
     *
     * @param eventKey clave unica de idempotencia
     * @return {@code true} si el evento ya fue procesado
     */
    Mono<Boolean> existsByEventKey(String eventKey);
}
