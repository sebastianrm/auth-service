package com.yanki.auth.domain.repository;

import com.yanki.auth.domain.model.Credential;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo de credenciales de acceso.
 *
 * <p>Solo se usan metodos derivados de Spring Data (sin {@code @Query}).</p>
 */
public interface CredentialRepository extends ReactiveMongoRepository<Credential, String> {

    /**
     * Busca la credencial de un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return credencial encontrada o vacio
     */
    Mono<Credential> findByUsername(String username);
}
