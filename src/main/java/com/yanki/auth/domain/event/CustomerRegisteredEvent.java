package com.yanki.auth.domain.event;

import java.util.List;

/**
 * Evento publicado por {@code customers-service} cuando un cliente se registra
 * en la plataforma ({@code yanki.customer.registered.v1}).
 *
 * <p>El servicio de autenticacion lo consume para crear la credencial del
 * cliente con la contrasena temporal hasheada con BCrypt.</p>
 *
 * @param customerId identificador del cliente registrado
 * @param username nombre de usuario de la credencial a crear
 * @param email correo electronico del cliente
 * @param temporaryPassword contrasena temporal en claro
 * @param roles roles del cliente
 * @param customerType tipo de cliente (PERSONAL, CORPORATE)
 * @param status estado del cliente (ACTIVE, BLOCKED, DELETED)
 */
public record CustomerRegisteredEvent(String customerId, String username, String email, String temporaryPassword, List<String> roles, String customerType, String status) {
}
