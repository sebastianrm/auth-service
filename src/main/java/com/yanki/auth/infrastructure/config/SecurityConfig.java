package com.yanki.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracion de seguridad del servicio.
 *
 * <p>Solo se incorporates el codificador de contrasenas BCrypt: el servicio
 * no instala un filtro de seguridad web porque la emision y validacion de
 * tokens se realiza de forma explicita en la capa de aplicacion.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Crea el codificador BCrypt usado para hashear y verificar contrasenas.
     *
     * @return codificador de contrasenas BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
