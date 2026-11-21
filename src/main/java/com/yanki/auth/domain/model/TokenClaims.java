package com.yanki.auth.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Claims extraidos de un token JWT validado.
 *
 * @param username identificador del sujeto del token ({@code sub})
 * @param customerId identificador del cliente al que pertenece la credencial
 * @param roles roles asignados al cliente
 * @param type tipo del token ({@code access} o {@code refresh})
 * @param issuedAt instante de emision del token
 * @param expiresAt instante de expiracion del token
 */
public record TokenClaims(String username, String customerId, List<String> roles, String type, Instant issuedAt, Instant expiresAt) {
}
