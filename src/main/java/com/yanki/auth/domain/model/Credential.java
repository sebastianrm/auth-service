package com.yanki.auth.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Credencial de acceso de un cliente de la plataforma Yanki.
 *
 * <p>Es el documento de la coleccion {@code credentials} de la base de datos
 * propia del servicio ({@code yanki_auth}). La contrasena nunca se persiste
 * en claro: solo se guarda su hash BCrypt en {@link #passwordHash}.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credentials")
public class Credential {

    /** Identificador del documento generado por MongoDB. */
    @Id
    private String id;

    /** Nombre de usuario unico con el que el cliente inicia sesion. */
    @Indexed(unique = true, name = "idx_unique_username")
    private String username;

    /** Hash BCrypt de la contrasena del usuario. */
    private String passwordHash;

    /** Identificador del cliente propietario de la credencial. */
    @Indexed(name = "idx_customerId")
    private String customerId;

    /** Correo electronico del cliente. */
    @Indexed(name = "idx_email")
    private String email;

    /** Roles asignados al cliente (por ejemplo {@code ROLE_CUSTOMER}). */
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    /** Indica si la credencial puede usarse para autenticarse. */
    private boolean enabled;

    /** Instante de creacion de la credencial. */
    private Instant createdAt;

    /** Instante de la ultima actualizacion de la credencial. */
    private Instant updatedAt;

    /** Instante del ultimo inicio de sesion exitoso. */
    private Instant lastLoginAt;
}
