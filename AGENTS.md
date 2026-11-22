# AGENTS.md — auth-service

## Agent Identity
- **Name**: `yanki-auth-agent`
- **Version**: 2.0.0
- **Service**: `auth-service`
- **Port**: `8081`
- **Partes del proyecto**: **I / II / III**
- **Estado esperado**: ✅ Implementado completamente
- **Objetivo de este documento**: permitir que un agente reconstruya el microservicio de forma autónoma, consistente con `README.md` raíz, `CASOS_USO`, `CASOS_PRUEBA`, `CONVENTIONS.md` y `docs/EVENTOS.md`

---

## 1. Misión del agente

Este agente debe ser capaz de **crear, mantener, validar y documentar** el microservicio `auth-service` sin depender de conocimiento implícito externo al repositorio.

El microservicio es responsable de:

1. **Autenticación JWT**
   - login
   - refresh token
   - validación de access token
   - logout por revocación de refresh token con blacklist en Redis

2. **Gestión de credenciales**
   - crear credenciales
   - listar credenciales
   - consultar credencial por ID
   - actualizar credencial
   - eliminar credencial

3. **Onboarding automático event-driven**
   - consumir `yanki.customer.registered.v1`
   - crear credencial inicial con contraseña temporal
   - garantizar idempotencia del consumo mediante `eventKey`

4. **Controles de seguridad**
   - hash BCrypt
   - blacklist de refresh tokens en Redis
   - invalidación de cache de credenciales en logout

---

## 2. Fuentes obligatorias de verdad

Al crear o ajustar este servicio, el agente debe alinear su implementación con estos artefactos:

| Fuente | Uso obligatorio |
|---|---|
| `README.md` raíz | arquitectura global, puertos, stack, integración con gateway, Kafka, Redis |
| `CONVENTIONS.md` | convenciones técnicas obligatorias del repositorio |
| `docs/EVENTOS.md` | contrato del tópico `yanki.customer.registered.v1`, naming de eventos, idempotencia |
| `CASOS_USO/CU-10-Autenticacion-JWT.md` | lógica funcional, actores, flujos y reglas de autenticación |
| `CASOS_PRUEBA/CASOS_PRUEBA_QA.md` | escenarios QA mínimos que la documentación y el diseño deben cubrir |
| `auth-service/README.md` | especificación funcional y operativa del servicio |
| `api-gateway/README.md` y `api-gateway/AGENTS.md` | compatibilidad JWT, endpoint de validación, issuer/secret compartidos |
| `init-mongo-parte1.js` | datos semilla esperados para credenciales de prueba |

---

## 3. Stack tecnológico obligatorio

El servicio debe respetar exactamente este stack:

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework base | Spring Boot | 3.3.5 |
| Cloud | Spring Cloud | 2023.0.3 |
| Web reactiva | Spring WebFlux | starter oficial |
| Reactividad de dominio | RxJava | 3.1.10 |
| Persistencia | Spring Data MongoDB Reactive | starter oficial |
| Caché | Spring Data Redis Reactive | starter oficial |
| Mensajería | Spring Kafka | starter oficial |
| Seguridad criptográfica | BCrypt | `spring-security-crypto` |
| JWT | JJWT | 0.12.6 |
| API Docs | springdoc-openapi | 2.6.0 |
| Resiliencia | Resilience4j | 2.2.0 |
| Calidad | Checkstyle | 10.21.1 |
| Cobertura | JaCoCo | 0.8.12 |
| Build | Maven | 3.9+ |

---

## 4. Restricciones arquitectónicas no negociables

1. **Database per service**
   - `auth-service` solo accede a `yanki_auth`
   - colecciones: `credentials`, `processed_events`

2. **Sin lógica bloqueante**
   - prohibido `block()`
   - prohibido `Thread.sleep()`
   - prohibido `subscribe()` en capa web

3. **Convención de paquetes**
   - `domain`
   - `infrastructure`
   - `interfaces`

4. **Cero configuración hardcodeada**
   - secretos JWT, issuer, TTLs, Kafka, Redis, Mongo, Eureka, Config Server por variables o Config Server

5. **Compatibilidad con api-gateway**
   - `JWT_SECRET` y `JWT_ISSUER` deben coincidir exactamente con `api-gateway`

6. **Comunicación**
   - REST inbound para auth y credenciales
   - Kafka inbound para onboarding
   - no produce eventos outbound

7. **Errores uniformes**
   - estructura `ApiError`
   - campos mínimos: `timestamp`, `status`, `error`, `code`, `message`, `path`, `details`

---

## 5. Alcance funcional obligatorio

### 5.1 Autenticación
El servicio debe implementar:

- login con `username` + `password`
- emisión de `accessToken`
- emisión de `refreshToken`
- refresh de access token
- validación de access token
- logout con blacklist de token en Redis e invalidación de cache

### 5.2 Credenciales
Debe existir CRUD completo de credenciales:

- crear
- listar
- consultar por identificador
- actualizar
- eliminar

### 5.3 Integración event-driven
Debe consumir:

- `yanki.customer.registered.v1`

Acción obligatoria:

- crear credencial automática inicial

### 5.4 Seguridad
Debe contemplar:

- hash BCrypt de contraseñas
- usuario deshabilitado (`enabled` flag)
- blacklist de refresh token en Redis
- invalidación de cache de credenciales en logout

> **Nota**: El servicio **no implementa** rate limiting ni bloqueo por intentos fallidos. Los campos `failedAttempts` y `lockedUntil` fueron eliminados del modelo `Credential`.

---

## 6. Endpoints REST implementados

### 6.1 Endpoints de autenticación

| Método | Ruta | Request | Response éxito | HTTP éxito |
|---|---|---|---|---|
| `POST` | `/api/auth/login` | `LoginRequest` | `TokenResponse` | `200` |
| `POST` | `/api/auth/refresh` | `RefreshTokenRequest` | `TokenResponse` | `200` |
| `POST` | `/api/auth/logout` | Header `Authorization: Bearer <token>` | `LogoutResponse` | `200` |
| `GET` | `/api/auth/validate` | Header `Authorization: Bearer <token>` | `TokenValidationResponse` | `200` |

### 6.2 Endpoints de credenciales

| Método | Ruta | Request | Response éxito | HTTP éxito |
|---|---|---|---|---|
| `POST` | `/api/auth/credentials` | `CredentialCreateRequest` | `CredentialResponse` | `201` |
| `GET` | `/api/auth/credentials` | — | `Flux<CredentialResponse>` | `200` |
| `GET` | `/api/auth/credentials/{id}` | — | `CredentialResponse` | `200` |
| `PUT` | `/api/auth/credentials/{id}` | `CredentialUpdateRequest` | `CredentialResponse` | `200` |
| `DELETE` | `/api/auth/credentials/{id}` | — | vacío | `204` |

---

## 7. Casos de éxito y error que deben quedar cubiertos

La documentación final debe cubrir explícitamente los escenarios derivados de `CU-10` y QA:

### 7.1 Casos de éxito mínimos
- login exitoso
- refresh exitoso
- validate exitoso
- logout exitoso con blacklist en Redis
- creación de credencial manual exitosa
- listado de credenciales exitoso
- consulta de credencial exitosa
- actualización de credencial exitosa
- eliminación de credencial exitosa
- creación automática de credencial por evento Kafka
- consumo duplicado ignorado por idempotencia

### 7.2 Casos de error mínimos
- credenciales inválidas
- cuenta deshabilitada
- token inválido
- token expirado
- credencial duplicada
- credencial inexistente
- request inválido por validación
- encabezado Authorization ausente o mal formado
- error de duplicidad en MongoDB
- evento duplicado ya procesado
- ruta no encontrada (404)
- método HTTP no permitido (405)

### 7.3 Códigos HTTP a documentar
Como mínimo, cubrir:
- `200`
- `201`
- `204`
- `400`
- `401`
- `404`
- `405`
- `409`
- `500`

---

## 8. Reglas de negocio obligatorias

### 8.1 Credenciales
- `username` debe ser único
- normalmente representa email en minúsculas
- password nunca se almacena en claro
- roles válidos:
  - `ROLE_CUSTOMER`
  - `ROLE_ENTERPRISE`
  - `ROLE_OPERATOR`
  - `ROLE_ADMIN`
- `enabled` controla acceso
- **No existe bloqueo por intentos fallidos** — los campos `failedAttempts` y `lockedUntil` fueron eliminados del modelo

### 8.2 JWT
- algoritmo: `HS256`
- access token TTL: `1h` (configurable vía `JWT_ACCESS_TOKEN_EXPIRATION`)
- refresh token TTL: `7d` (configurable vía `JWT_REFRESH_TOKEN_EXPIRATION`)
- issuer compartido con gateway
- secret mínimo de 32 caracteres
- validate debe devolver claims suficientes para propagación por gateway
- `TokenResponse` incluye `username` y `roles` del cliente

### 8.3 Logout
- parsea el access token para obtener el username
- agrega el token a la blacklist en Redis con TTL igual al tiempo restante de expiración
- invalida la cache de credenciales del usuario en Redis
- devuelve `LogoutResponse` con mensaje de confirmación

### 8.4 Onboarding automático
Cuando llega `yanki.customer.registered.v1`:
1. leer `customerId`, `username`, `email`, `temporaryPassword`, `roles`, `customerType`, `status`
2. construir clave idempotente: `<topic>:<customerId>:<username>`
3. si ya existe en `processed_events` por `eventKey`, ignorar
4. si no existe:
   - hashear contraseña temporal
   - crear credencial
   - marcar evento como procesado
   - invalidar cache

### 8.5 Riesgos conocidos a conservar en documentación
- `temporaryPassword` viaja en claro por Kafka
- `JWT_ISSUER` puede diferir entre gateway y auth si no se configura bien
- el evento `CustomerRegisteredEvent` incluye `customerType` y `status` que no se usan directamente en la creación de la credencial

---

## 9. Modelo de datos obligatorio

### 9.1 Colección `credentials`
Campos mínimos esperados:
- `id`
- `username` (único, indexado)
- `passwordHash`
- `customerId` (indexado)
- `email` (indexado)
- `roles`
- `enabled`
- `createdAt` (Instant)
- `updatedAt` (Instant)
- `lastLoginAt` (Instant)

> **Nota**: Los campos `failedAttempts` y `lockedUntil` **no existen** en el modelo actual.

### 9.2 Colección `processed_events`
Campos mínimos esperados:
- `id`
- `eventKey` (único, indexado) — formato: `<topic>:<customerId>:<username>`
- `topic`
- `entityId`
- `processedAt` (Instant)

### 9.3 Índices esperados
- índice único en `credentials.username` (`idx_unique_username`)
- índice en `credentials.customerId` (`idx_customerId`)
- índice en `credentials.email` (`idx_email`)
- índice único en `processed_events.eventKey`

---

## 10. Redis obligatorio

Claves esperadas:

| Clave | Uso |
|---|---|
| `yanki:auth:credential:{username}` | caché de credencial (TTL configurable, default 15m) |
| `yanki:auth:blacklist:{token}` | blacklist de tokens revocados por logout (TTL = tiempo restante del token) |

Comportamiento:
- invalidar cache al crear/actualizar/eliminar credenciales
- invalidar cache al hacer logout
- blacklist con TTL igual al tiempo restante de expiración del token
- no almacenar tokens ya expirados en la blacklist

> **Nota**: No se usan claves de rate limiting en la implementación actual.

---

## 11. Kafka obligatorio

### Topic consumido
- `yanki.customer.registered.v1`

### Contrato esperado del payload
Debe incluir al menos:
- `customerId`
- `username`
- `email`
- `temporaryPassword`
- `roles`
- `customerType` (PERSONAL, CORPORATE)
- `status` (ACTIVE, BLOCKED, DELETED)

### Idempotencia
- La clave de idempotencia es `<topic>:<customerId>:<username>`
- Se almacena en el campo `eventKey` de `processed_events`
- El repositorio verifica existencia mediante `existsByEventKey`

---

## 12. Variables de entorno mínimas

| Variable | Obligatoria | Observación |
|---|---|---|
| `SERVER_PORT` | sí | default `8081` |
| `MONGODB_URI` | sí | BD `yanki_auth` |
| `REDIS_HOST` | sí | Redis reactivo |
| `REDIS_PORT` | sí | default `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | sí | brokers Kafka |
| `KAFKA_GROUP_ID` | sí | consumer group auth |
| `EUREKA_URL` | sí | service discovery |
| `CONFIG_SERVER_URL` | sí | config centralizada |
| `JWT_SECRET` | sí | debe coincidir con gateway, mínimo 32 caracteres |
| `JWT_ISSUER` | sí | default `yanki-platform`, debe coincidir con gateway |
| `JWT_ACCESS_TOKEN_EXPIRATION` | no | default `1h` |
| `JWT_REFRESH_TOKEN_EXPIRATION` | no | default `7d` |
| `CREDENTIAL_CACHE_TTL` | no | default `15m` |
| `CREDENTIAL_CACHE_KEY_PREFIX` | no | default `yanki:auth:credential:` |

---

## 13. Estructura esperada del proyecto

```text
auth-service/
├── pom.xml
├── Dockerfile
├── checkstyle.xml
├── README.md
├── AGENTS.md
└── src/
    ├── main/
    │   ├── java/com/yanki/auth/
    │   │   ├── AuthServiceApplication.java
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Credential.java
    │   │   │   │   ├── CredentialCreateCommand.java
    │   │   │   │   ├── CredentialUpdateCommand.java
    │   │   │   │   ├── IssuedTokens.java
    │   │   │   │   ├── LoginCommand.java
    │   │   │   │   ├── ProcessedEvent.java
    │   │   │   │   ├── RefreshCommand.java
    │   │   │   │   ├── TokenClaims.java
    │   │   │   │   └── TokenValidationResult.java
    │   │   │   ├── repository/
    │   │   │   │   ├── CredentialRepository.java
    │   │   │   │   └── ProcessedEventRepository.java
    │   │   │   ├── service/
    │   │   │   │   ├── AuthService.java
    │   │   │   │   ├── CredentialCacheService.java
    │   │   │   │   ├── CredentialService.java
    │   │   │   │   └── JwtService.java
    │   │   │   ├── event/
    │   │   │   │   ├── CustomerRegisteredEvent.java
    │   │   │   │   └── Topics.java
    │   │   │   └── exception/
    │   │   │       └── BusinessException.java
    │   │   ├── infrastructure/
    │   │   │   ├── config/
    │   │   │   │   ├── CacheProperties.java
    │   │   │   │   ├── JwtProperties.java
    │   │   │   │   └── SecurityConfig.java
    │   │   │   ├── cache/
    │   │   │   │   └── TokenBlacklistService.java
    │   │   │   ├── messaging/
    │   │   │   │   └── CredentialConsumer.java
    │   │   │   └── persistence/
    │   │   │       └── ReactiveAdapter.java
    │   │   └── interfaces/
    │   │       └── rest/
    │   │           ├── GlobalExceptionHandler.java
    │   │           ├── controller/
    │   │           │   ├── AuthController.java
    │   │           │   └── CredentialController.java
    │   │           └── dto/
    │   │               ├── ApiError.java
    │   │               ├── CredentialCreateRequest.java
    │   │               ├── CredentialResponse.java
    │   │               ├── CredentialUpdateRequest.java
    │   │               ├── LoginRequest.java
    │   │               ├── LogoutResponse.java
    │   │               ├── RefreshTokenRequest.java
    │   │               ├── TokenResponse.java
    │   │               └── TokenValidationResponse.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
            └── com/yanki/auth/
                ├── domain/service/
                │   ├── AuthServiceTest.java
                │   ├── CredentialCacheServiceTest.java
                │   ├── CredentialServiceTest.java
                │   └── JwtServiceTest.java
                ├── infrastructure/messaging/
                │   └── CredentialConsumerTest.java
                └── interfaces/rest/controller/
                    ├── AuthControllerTest.java
                    ├── CredentialControllerTest.java
                    └── GlobalExceptionHandlerTest.java
```

---

## 14. Calidad mínima exigida

El agente debe dejar el servicio apto para:

```bash
mvn clean verify
```

Debe incluir:
- compilación correcta
- checkstyle sin errores
- tests unitarios y/o integración según el alcance real
- reporte JaCoCo generado

---

## 15. Cobertura funcional derivada de QA

Del catálogo `CASOS_PRUEBA/CASOS_PRUEBA_QA.md`, este servicio debe poder soportar o documentar al menos:

| Caso | Descripción | Estado esperado |
|---|---|---|
| `CP-052` | autenticación válida y emisión de JWT | soportado |
| `CP-053` | rechazo con credenciales inválidas | soportado |
| `CP-054` | acceso a endpoint protegido sin JWT | coordinado con gateway |
| `CP-055` | acceso con JWT inválido o expirado | soportado junto con gateway |
| validación de onboarding | creación de credencial automática por evento | soportado |
| idempotencia | no duplicar credenciales ante reproceso | soportado |

Si algún caso depende del gateway, debe quedar explícito en la documentación.

---

## 16. Guía operativa para reconstrucción autónoma

Un agente autónomo debe poder seguir esta secuencia:

1. crear `pom.xml` con el stack exacto
2. crear `application.yml` con Mongo, Redis, Kafka, Config y Eureka
3. modelar `Credential` y `ProcessedEvent`
4. crear repositorios reactivos Mongo
5. implementar servicios:
   - `JwtService`
   - `AuthService`
   - `CredentialService`
   - `CredentialCacheService`
   - `TokenBlacklistService`
6. implementar DTOs request/response
7. implementar `AuthController` con login, refresh, logout, validate
8. implementar `CredentialController`
9. implementar `GlobalExceptionHandler`
10. implementar consumidor Kafka `customer.registered`
11. documentar errores, HTTP codes, env vars y riesgos conocidos
12. asegurar compatibilidad con `api-gateway`
13. dejar pruebas y build pasando

---

## 17. Códigos de error implementados

| Código | HTTP | Causa |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | Username o password incorrectos |
| `INVALID_TOKEN` | 401 | Header Authorization ausente, mal formado o firma inválida |
| `TOKEN_EXPIRED` | 401 | Access token expirado |
| `CREDENTIAL_NOT_FOUND` | 404 | Credencial no encontrada por ID |
| `CREDENTIAL_ALREADY_EXISTS` | 409 | Username duplicado o error de clave duplicada en MongoDB |
| `VALIDATION_ERROR` | 400 | Campos del request inválidos |
| `INVALID_REQUEST` | 400 | Petición mal formada |
| `ROUTE_NOT_FOUND` | 404 | Ruta no registrada en el servicio |
| `METHOD_NOT_ALLOWED` | 405 | Método HTTP no permitido en la ruta |
| `INTERNAL_ERROR` | 500 | Error inesperado no controlado |

---

## 18. Resultado esperado de una documentación correcta

`README.md` y `AGENTS.md` del servicio deben permitir que otro agente:

- entienda el propósito del servicio
- conozca el stack exacto
- implemente todos los endpoints requeridos
- modele entidades y colecciones
- conecte Mongo, Redis, Kafka, Eureka y Config Server
- conozca reglas de negocio, errores y códigos HTTP
- contemple riesgos conocidos
- sepa qué casos de uso y QA debe satisfacer
- alinee compatibilidad con `api-gateway`
- pueda reconstruir el microservicio sin acudir a conocimiento externo adicional

---

## 19. Referencias cruzadas obligatorias

- `README.md`
- `CONVENTIONS.md`
- `docs/EVENTOS.md`
- `CASOS_USO/CU-10-Autenticacion-JWT.md`
- `CASOS_PRUEBA/CASOS_PRUEBA_QA.md`
- `auth-service/README.md`
- `api-gateway/README.md`
- `api-gateway/AGENTS.md`
