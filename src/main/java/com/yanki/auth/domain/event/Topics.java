package com.yanki.auth.domain.event;

/**
 * Topics Kafka del contrato de eventos de la plataforma Yanki.
 */
public final class Topics {

    /** Evento de registro de cliente publicado por {@code customers-service}. */
    public static final String CUSTOMER_REGISTERED = "yanki.customer.registered.v1";

    /**
     * Constructor privado: la clase es un contenedor de constantes.
     */
    private Topics() {
    }
}
