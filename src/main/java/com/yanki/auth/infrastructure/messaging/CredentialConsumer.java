package com.yanki.auth.infrastructure.messaging;

import com.yanki.auth.domain.event.CustomerRegisteredEvent;
import com.yanki.auth.domain.event.Topics;
import com.yanki.auth.domain.service.CredentialService;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka de eventos de registro de cliente.
 *
 * <p>Crea la credencial del cliente con la contrasena temporal hasheada con
 * BCrypt. El consumo es idempotente: un evento repetido no duplica la
 * credencial.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CredentialConsumer {

    /** Servicio de credenciales de acceso. */
    private final CredentialService credentialService;

    /**
     * Procesa el evento de registro de cliente publicado por {@code customers-service}.
     *
     * @param event evento de registro de cliente
     */
    @KafkaListener(
            topics = Topics.CUSTOMER_REGISTERED,
            groupId = "${spring.kafka.consumer.group-id}",
            properties = "spring.json.value.default.type=com.yanki.auth.domain.event.CustomerRegisteredEvent")
    public void onCustomerRegistered(CustomerRegisteredEvent event) {
        try {
            processRegistration(event).toFuture().get(30, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Credencial del cliente procesada exitosamente");
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Error creando la credencial del cliente: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new RuntimeException("Error procesando evento de registro: " + e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            log.error("Error procesando evento de registro: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando evento de registro: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta la creacion idempotente de la credencial a partir del evento.
     *
     * @param event evento de registro de cliente
     * @return completable cuando la credencial queda creada o validada
     */
    public Completable processRegistration(CustomerRegisteredEvent event) {
        return credentialService.registerFromEvent(event).ignoreElement();
    }
}
