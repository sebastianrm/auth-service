# AGENTS.md — auth-service

## Agent Identity
- **Name**: `yanki-auth-agent`
- **Version**: 1.1.0
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
   - logout por revocación de refresh token

2. **Gestión de credenciales**
   - crear credenciales
   - listar credenciales
   - consultar credencial
   - actualizar credencial
   - eliminar credencial

3. **Onboarding automático event-driven**
   - consumir `yanki.customer.registered.v1`
   - crear credencial inicial con contraseña temporal
   - garantizar idempotencia del consumo

4. **Controles de seguridad**
   - hash BCrypt
   - rate limit de login
   - bloqueo por intentos fallidos
   - blacklist de refresh tokens en Redis

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
- revocación de refresh token

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
- usuario deshabilitado
- bloqueo temporal por intentos fallidos
- blacklist de refresh token en Redis
- rate limiting de login por IP + username

---

## 6. Endpoints REST obligatorios

> Importante: la documentación histórica del servicio tenía rutas desalineadas. El código real observado expone `/api/auth/credentials`, no `/api/credentials`. El agente debe documentar y construir según el código real o alinear código y documentación explícitamente.

### 6.1 Endpoints de autenticación

| Método | Ruta | Request | Response éxito | HTTP éxito |
|---|---|---|---|---|
| `POST` | `/api/auth/login` | `LoginRequest` | `TokenResponse` | `200` |
| `POST` | `/api/auth/refresh` | `RefreshTokenRequest` | `TokenResponse` | `200` |
| `GET` | `/api/auth/validate` | Header `Authorization: Bearer <token>` | `TokenValidationResponse` | `200` |

### 6.2 Endpoints de credenciales

| Método | Ruta | Request | Response éxito | HTTP éxito |
|---|---|---|---|---|
| `POST` | `/api/auth/credentials` | `CredentialCreateRequest` | `CredentialResponse` | `201` |
| `GET` | `/api/auth/credentials` | — | `Flux<CredentialResponse>` | `200` |
| `GET` | `/api/auth/credentials/{id}` | — | `CredentialResponse` | `200` |
| `PUT` | `/api/auth/credentials/{id}` | `CredentialUpdateRequest` | `CredentialResponse` | `200` |
| `DELETE` | `/api/auth/credentials/{id}` | — | vacío | `204` |

### 6.3 Endpoint faltante vs documentación previa

La documentación previa hablaba de `logout`, pero el `AuthController` leído no lo expone.  
Por tanto, el agente debe hacer una de estas dos cosas:

1. **Si el servicio debe cumplir CU-10 completo**, agregar `POST /api/auth/logout`.
2. **Si el servicio se mantiene como está**, corregir README y AGENTS para no documentar un endpoint inexistente.

No se debe dejar una discrepancia entre código y documentación.

---

## 7. Casos de éxito y error que deben quedar cubiertos

La documentación final debe cubrir explícitamente los escenarios derivados de `CU-10` y QA:

### 7.1 Casos de éxito mínimos
- login exitoso
- refresh exitoso
- validate exitoso
- creación de credencial manual exitosa
- listado de credenciales exitoso
- consulta de credencial exitosa
- actualización de credencial exitosa
- eliminación de credencial exitosa
- creación automática de credencial por evento Kafka
- consumo duplicado ignorado por idempotencia

### 7.2 Casos de error mínimos
- credenciales inválidas
- cuenta bloqueada
- cuenta deshabilitada
- token inválido
- token expirado
- refresh token revocado
- credencial duplicada
- credencial inexistente
- request inválido por validación
- encabezado Authorization ausente o mal formado
- error de duplicidad en MongoDB
- evento duplicado ya procesado

### 7.3 Códigos HTTP a documentar
Como mínimo, cubrir:
- `200`
- `201`
- `204`
- `400`
- `401`
- `403`
- `404`
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
- intentos fallidos incrementan contador
- al superar máximo se bloquea temporalmente

### 8.2 JWT
- algoritmo: `HS256`
- access token TTL: `15m`
- refresh token TTL: `7d`
- issuer compartido con gateway
- secret mínimo de 32 caracteres
- validate debe devolver claims suficientes para propagación por gateway

### 8.3 Onboarding automático
Cuando llega `yanki.customer.registered.v1`:
1. leer `customerId`, `username`, `email`, `temporaryPassword`, `roles`
2. construir clave idempotente
3. si ya existe en `processed_events`, ignorar
4. si no existe:
   - hashear contraseña temporal
   - crear credencial
   - marcar evento como procesado
   - opcionalmente actualizar caché

### 8.4 Riesgos conocidos a conservar en documentación
- `temporaryPassword` viaja en claro por Kafka
- hay inconsistencia entre la idempotencia exigida por `docs/EVENTOS.md` y la implementada real
- `JWT_ISSUER` puede diferir entre gateway y auth si no se configura bien
- la documentación histórica mencionó endpoints distintos a los realmente implementados

---

## 9. Modelo de datos obligatorio

### 9.1 Colección `credentials`
Campos mínimos esperados:
- `id`
- `username`
- `passwordHash`
- `customerId`
- `email`
- `roles`
- `enabled`
- `failedAttempts`
- `lockedUntil`
- `lastLoginAt`
- `createdAt`
- `updatedAt`

### 9.2 Colección `processed_events`
Campos mínimos esperados:
- `id`
- `topic`
- `entityId`
- `username`
- `processedAt`

### 9.3 Índices esperados
- índice único en `credentials.username`
- unicidad efectiva en `processed_events.id`

---

## 10. Redis obligatorio

Claves esperadas:

| Clave | Uso |
|---|---|
| `yanki:auth:credential:{username}` | caché de credencial |
| `yanki:auth:ratelimit:{ip}:{user}` | control de intentos/login |
| `yanki:auth:revoked:{refreshToken}` | blacklist refresh token |

Comportamiento:
- invalidar o refrescar caché al crear/actualizar/eliminar credenciales
- blacklist con TTL igual al refresh token
- rate limit tipo sliding window o equivalente documentado

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

### Idempotencia
- `docs/EVENTOS.md` exige `<topic>:<entityId>:<occurredAt>`
- implementación documentada actual usa `<topic>:<customerId>:<username>`

El agente debe:
1. documentar la diferencia
2. no ocultar la inconsistencia
3. decidir si alinea código o mantiene compatibilidad explícita

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
| `JWT_SECRET` | sí | debe coincidir con gateway |
| `JWT_ISSUER` | sí | debe coincidir con gateway |
| `JWT_ACCESS_TTL` | sí | default `15m` |
| `JWT_REFRESH_TTL` | sí | default `7d` |
| `YANKI_AUTH_MAX_FAILED_ATTEMPTS` | sí | default `5` |
| `YANKI_AUTH_LOCKOUT_DURATION` | sí | default `15m` |

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
    │   │   │   ├── repository/
    │   │   │   ├── service/
    │   │   │   ├── event/
    │   │   │   └── exception/
    │   │   ├── infrastructure/
    │   │   │   ├── config/
    │   │   │   ├── messaging/
    │   │   │   └── persistence/
    │   │   └── interfaces/
    │   │       └── rest/
    │   │           ├── controller/
    │   │           ├── dto/
    │   │           └── GlobalExceptionHandler.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
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
   - caché/rate limiting si aplica
6. implementar DTOs request/response
7. implementar `AuthController`
8. implementar `CredentialController`
9. implementar `GlobalExceptionHandler`
10. implementar consumidor Kafka `customer.registered`
11. documentar errores, HTTP codes, env vars y riesgos conocidos
12. asegurar compatibilidad con `api-gateway`
13. dejar pruebas y build pasando

---

## 17. Brechas detectadas al validar la documentación actual

Estas brechas deben considerarse abiertas hasta que README y código estén alineados:

1. **Ruta de credenciales inconsistente**
   - README documenta `/api/credentials`
   - código real expone `/api/auth/credentials`

2. **Consulta/actualización por username inconsistente**
   - README habla de `{username}`
   - código real usa `{id}`

3. **Método de validate inconsistente**
   - README documentaba `POST /api/auth/validate`
   - código real expone `GET /api/auth/validate` con header Authorization

4. **Logout documentado pero no observado en controller**
   - posible brecha entre CU-10 y el código real

5. **Falta de casos HTTP completos**
   - la documentación previa no cubría exhaustivamente `400`, `404`, `500`

6. **Documentación insuficiente para reconstrucción autónoma**
   - faltaba señalar dependencias obligatorias con `README.md` raíz, QA y casos de uso

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
