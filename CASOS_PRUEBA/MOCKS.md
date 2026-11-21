# Estrategia de Mocks — auth-service

## Objetivo

Definir cómo aislar `auth-service` de sistemas externos para probar exclusivamente su funcionamiento.

## Dependencias externas a mockear

### 1. MongoDB
Mockear:
- `CredentialRepository`
- `ProcessedEventRepository`

Cobertura:
- búsqueda de credencial
- creación de credencial
- actualización de credencial
- eliminación de credencial
- verificación de duplicados
- persistencia de eventos procesados

Herramienta sugerida:
- Mockito

---

### 2. Redis
Mockear comportamientos de:
- blacklist de refresh tokens
- rate limiting login
- cache de credenciales

Cobertura:
- refresh token revocado
- login con límite excedido
- invalidación/actualización de cache

Herramienta sugerida:
- interfaces/dobles de prueba o servicios mockeados

---

### 3. Kafka
Mockear:
- payload de `CustomerRegisteredEvent`
- invocación del consumer
- persistencia posterior al consumo

Cobertura:
- onboarding automático exitoso
- reproceso de evento duplicado
- error controlado durante consumo

Herramienta sugerida:
- invocar directamente el consumer o servicio de aplicación con eventos simulados

---

### 4. Config Server / Eureka
No forman parte del comportamiento funcional del microservicio.

Estrategia:
- usar configuración local de test
- deshabilitar registro/descubrimiento en pruebas
- no probar conectividad real

---

### 5. API Gateway
No se mockea como sistema completo.

Se valida solamente:
- que `auth-service` acepte el contrato esperado del endpoint `/api/auth/validate`
- que el response contenga claims útiles para propagación

---

## Configuración sugerida para tests

### Unit tests
Mockear todo:
- repositorios
- servicios auxiliares
- caches
- validadores externos

### Controller tests
Usar:
- `WebTestClient`
- servicios mockeados

### Service tests
Usar:
- Mockito
- StepVerifier

### Consumer tests
Usar:
- evento simulado
- repositorios mockeados
- validación de idempotencia

---

## Casos donde aplicar mocks

| Caso | Mocks requeridos |
|---|---|
| Login exitoso | repositorio credencial, BCrypt/JWT service, cache/rate limit |
| Login inválido | repositorio credencial |
| Cuenta bloqueada | repositorio credencial |
| Cuenta deshabilitada | repositorio credencial |
| Refresh exitoso | JWT service, blacklist Redis, repositorio credencial |
| Token inválido | JWT service |
| Credencial duplicada | repositorio credencial |
| Credencial no encontrada | repositorio credencial |
| Onboarding automático | consumer + repositorios + cache |
| Idempotencia duplicada | processed event repository |

---

## Recomendación de fixtures

Crear fixtures reutilizables para:

- `LoginRequest`
- `RefreshTokenRequest`
- `CredentialCreateRequest`
- `CredentialUpdateRequest`
- `CredentialResponse`
- `TokenResponse`
- `TokenValidationResponse`
- `CustomerRegisteredEvent`

---

## Resultado esperado

Las pruebas del directorio `auth-service/CASOS_PRUEBA` deben poder diseñarse y ejecutarse sin depender de:

- Mongo real
- Redis real
- Kafka real
- Gateway real
- Customers real
