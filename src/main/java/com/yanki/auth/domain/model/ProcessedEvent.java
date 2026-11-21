package com.yanki.auth.domain.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Marca de idempotencia de un evento Kafka ya procesado.
 *
 * <p>Se persiste en la coleccion local {@code processed_events} con una clave
 * unica {@link #eventKey} construida como
 * {@code <topic>:<entityId>:<entityKey>}. Si la clave ya existe, el evento
 * se descarta de forma silenciosa.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processed_events")
public class ProcessedEvent {

    /** Identificador del documento generado por MongoDB. */
    @Id
    private String id;

    /** Clave unica de idempotencia del evento. */
    @Indexed(unique = true)
    private String eventKey;

    /** Topic Kafka del evento procesado. */
    private String topic;

    /** Identificador de la entidad de negocio afectada por el evento. */
    private String entityId;

    /** Instante en que se registro el evento como procesado. */
    private Instant processedAt;
}
