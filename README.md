# auth-service

## 🔐 ¿Qué es?

El **auth-service** es el **centro de autenticación** de la plataforma bancaria Yanki. Imagínalo como el departamento de seguridad de un edificio: verifica quién eres, te entrega un pase (token JWT) para entrar, y puede cancelar ese pase cuando decides salir.

Es el único servicio que conoce las contraseñas de los usuarios y es el encargado de emitir los tokens que el `api-gateway` usa para permitir o denegar el acceso al resto de la plataforma.

- **Puerto:** `8081`
- **Base de datos:** MongoDB `yanki_auth` (colecciones: `credentials`, `processed_events`)
- **Cache:** Redis (credenciales en cache, token blacklist)
- **Eventos:** Kafka (consume `yanki.customer.registered.v1`)
- **Stack:** Java 17, Spring Boot 3.3.5, Spring WebFlux, MongoDB Reactive, Redis Reactive, RxJava 3, Spring Kafka, JJWT

---

## 1. ¿Qué hace?

| Responsabilidad | Qué significa en la práctica |
|---|---|
| **Login** | Verifica usuario y contraseña → emite access token (1h) + refresh token (7 días) |
| **Logout** | Revoca el token guardándolo en Redis (blacklist) y limpia la cache de credenciales |
| **Refresh** | Emite un nuevo access token usando un refresh token válido |
| **Validación** | Valida un access token y devuelve sus claims (usado por api-gateway) |
| **CRUD Credenciales** | Admin puede crear, listar, leer, actualizar y eliminar credenciales |
| **Onboarding automático** | Cuando se registra un cliente, crea su credencial con contraseña temporal |
| **Token Blacklist** | Almacena tokens revocados en Redis para que el gateway los rechace |

---

## 2. Arquitectura de Alto Nivel

```mermaid
graph TB
    subgraph Cliente
        A[App Móvil / Web]
    end

    subgraph "API Gateway"
        B[Gateway<br/>Puerto 8080]
    end

    subgraph "Auth Service (Puerto 8081)"
        C[AuthController]
        D[AuthService]
        E[JwtService]
        F[CredentialService]
        G[TokenBlacklistService]
        H[CredentialConsumer]
    end

    subgraph MongoDB
        I[(credentials)]
        J[(processed_events)]
    end

    subgraph Redis
        K[(Token Blacklist<br/>yanki:auth:blacklist:*)]
        L[(Credenciales Cache<br/>yanki:auth:credential:*)]
    end

    subgraph Kafka
        M[yanki.customer.registered.v1]
    end

    subgraph Customers Service
        N[customers-service]
    end

    A -->|HTTP| B
    B -->|lb://auth-service| C
    C --> D
    D --> E
    D --> F
    D --> G
    F --> I
    F --> L
    G --> K
    H --> M
    N -->|publica evento| M
    M --> H
    H --> F
    F --> J
```

---

## 3. Flujo de Login

### 3.1 ¿Qué pasa cuando un usuario inicia sesión?

```mermaid
sequenceDiagram
    autonumber
    participant Cliente
    participant Gateway as API Gateway
    participant Controller as AuthController
    participant AuthService as AuthService
    participant CredService as CredentialService
    participant JwtService as JwtService
    participant MongoDB as MongoDB
    participant Redis as Redis

    Cliente->>Gateway: POST /api/auth/login<br/>{username, password}

    Gateway->>Gateway: ¿Ruta pública?
    Note over Gateway: /api/auth/** → SÍ<br/>Sin verificar JWT

    Gateway->>Controller: POST /api/auth/login

    Controller->>AuthService: login(LoginCommand)

    AuthService->>CredService: findByUsername(username)

    CredService->>Redis: GET yanki:auth:credential:<username>

    alt Cache hit
        Redis-->>CredService: Credencial JSON
    else Cache miss
        Redis-->>CredService: null
        CredService->>MongoDB: findOne por username
        MongoDB-->>CredService: Documento credencial
        CredService->>Redis: SET credencial con TTL
    end

    CredService-->>AuthService: Credential

    alt Credencial no encontrada
        AuthService-->>Controller: INVALID_CREDENTIALS
        Controller-->>Gateway: 401
        Gateway-->>Cliente: 401 INVALID_CREDENTIALS
    else Contraseña incorrecta
        AuthService-->>Controller: INVALID_CREDENTIALS
        Controller-->>Gateway: 401
        Gateway-->>Cliente: 401 INVALID_CREDENTIALS
    else Credencial deshabilitada
        AuthService-->>Controller: INVALID_CREDENTIALS
        Controller-->>Gateway: 401
        Gateway-->>Cliente: 401 INVALID_CREDENTIALS
    else Credencial válida
        AuthService->>CredService: recordLogin(credential)
        CredService->>MongoDB: actualizar lastLoginAt
        CredService->>Redis: actualizar cache

        AuthService->>JwtService: generateAccessToken(credential)
        JwtService-->>AuthService: access token JWT

        AuthService->>JwtService: generateRefreshToken(credential)
        JwtService-->>AuthService: refresh token JWT

        AuthService-->>Controller: IssuedTokens

        Controller-->>Gateway: 200 TokenResponse
        Gateway-->>Cliente: 200 {accessToken, refreshToken, tokenType, expiresIn, username, roles}
    end
```

### 3.2 Estructura del Token JWT

Un access token JWT contiene los siguientes **claims** (información embebida):

| Claim | Descripción | Ejemplo |
|---|---|---|
| `sub` | Username del usuario | `operador@yanki.com` |
| `customerId` | ID del cliente | `OPERATOR-001` |
| `roles` | Lista de roles | `["ROLE_OPERATOR"]` |
| `type` | Tipo de token | `access` |
| `iss` | Emisor del token | `yanki-platform` |
| `iat` | Fecha de emisión | `1794962678` (timestamp) |
| `exp` | Fecha de expiración | `1794966278` (timestamp) |

---

## 4. Flujo de Logout con Token Blacklist

### 4.1 ¿Qué pasa cuando un usuario cierra sesión?

**Problema que resuelve:** Los tokens JWT son *stateless* (sin estado). Una vez emitidos, son criptográficamente válidos hasta su expiración natural. Sin un mecanismo de revocación, un usuario que hace logout podría seguir usando su token durante toda la vigencia (1 hora).

**Solución:** El token se guarda en Redis con el tiempo restante de expiración como TTL. El api-gateway consulta esta lista en cada petición.

```mermaid
sequenceDiagram
    autonumber
    participant Cliente
    participant Gateway as API Gateway
    participant Controller as AuthController
    participant AuthService as AuthService
    participant JwtService as JwtService
    participant Blacklist as TokenBlacklistService
    participant CredService as CredentialService
    participant Redis as Redis

    Cliente->>Controller: POST /api/auth/logout<br/>Authorization: Bearer <accessToken>

    Controller->>Controller: Extraer token del header

    alt Header ausente o mal formado
        Controller-->>Cliente: 401 INVALID_TOKEN
    else Header válido
        Controller->>AuthService: logout(accessToken)

        AuthService->>JwtService: parseToken(accessToken, "access")
        JwtService-->>AuthService: TokenClaims<br/>(username, expiresAt)

        AuthService->>Blacklist: blacklistToken(token, expiresAt)

        Blacklist->>Blacklist: Calcular TTL restante<br/>(expiresAt - ahora)

        alt Token ya expirado
            Blacklist-->>AuthService: Completado (sin almacenar)
        else Token con tiempo restante
            Blacklist->>Redis: SET yanki:auth:blacklist:<token><br/>TTL = tiempo restante
            Redis-->>Blacklist: OK
            Blacklist-->>AuthService: Completado
        end

        AuthService->>CredService: evictCache(username)
        CredService->>Redis: DEL yanki:auth:credential:<username>
        Redis-->>CredService: OK

        AuthService-->>Controller: Completado
        Controller-->>Cliente: 200 {message, success}
    end
```

### 4.2 ¿Qué pasa si el usuario intenta usar el token después del logout?

```mermaid
sequenceDiagram
    autonumber
    participant Cliente
    participant Gateway as API Gateway
    participant Filter as Filtro JWT
    participant Validator as JwtTokenValidator
    participant Blacklist as TokenBlacklistService
    participant Redis as Redis

    Cliente->>Gateway: GET /api/accounts<br/>Authorization: Bearer <token_revocado>

    Gateway->>Filter: filter(exchange, chain)

    Filter->>Validator: validateReactive(token)

    Validator->>Validator: Verificar firma HS256 ✓
    Validator->>Validator: Verificar issuer ✓
    Validator->>Validator: Verificar expiración ✓

    Note over Validator: Firma válida, no expirado...<br/>¿Pero fue revocado?

    Validator->>Blacklist: isBlacklisted(token)

    Blacklist->>Redis: EXISTS yanki:auth:blacklist:<token>
    Redis-->>Blacklist: true (encontrado)

    Blacklist-->>Validator: true (revocado)
    Validator-->>Filter: Mono.empty()

    Filter-->>Cliente: 401 INVALID_TOKEN<br/>(Token revocado por logout)
```

---

## 5. Flujo de Refresh Token

Cuando el access token expira (1 hora), el cliente puede usar el refresh token (vigencia de 7 días) para obtener un nuevo access token sin volver a ingresar la contraseña.

```mermaid
sequenceDiagram
    autonumber
    participant Cliente
    participant Controller as AuthController
    participant AuthService as AuthService
    participant JwtService as JwtService
    participant CredService as CredentialService
    participant MongoDB as MongoDB

    Cliente->>Controller: POST /api/auth/refresh<br/>{refreshToken}

    Controller->>AuthService: refresh(RefreshCommand)

    AuthService->>JwtService: parseToken(refreshToken, "refresh")
    JwtService-->>AuthService: TokenClaims

    AuthService->>CredService: findByUsername(username)
    CredService->>MongoDB: findOne por username
    MongoDB-->>CredService: Credential

    alt Credencial deshabilitada
        AuthService-->>Controller: INVALID_CREDENTIALS
        Controller-->>Cliente: 401
    else Credencial válida
        AuthService->>JwtService: generateAccessToken(credential)
        JwtService-->>AuthService: nuevo access token

        AuthService-->>Controller: IssuedTokens
        Controller-->>Cliente: 200 {accessToken, refreshToken, ...}
    end
```

---

## 6. Onboarding Automático (Event-Driven)

Cuando un nuevo cliente se registra en `customers-service`, se publica un evento en Kafka. El auth-service lo consume y crea automáticamente la credencial del cliente.

```mermaid
sequenceDiagram
    autonumber
    participant Cliente
    participant CustService as customers-service
    participant Kafka as Kafka Broker
    participant Consumer as CredentialConsumer
    participant CredService as CredentialService
    participant MongoDB as MongoDB

    Cliente->>CustService: POST /api/customers<br/>(Registro nuevo)

    CustService->>MongoDB: Guardar cliente
    CustService->>Kafka: Publicar yanki.customer.registered.v1

    Kafka->>Consumer: Entregar evento

    Consumer->>CredService: registerFromEvent(event)

    CredService->>MongoDB: ¿Existe eventKey?<br/>clave: <topic>:<customerId>:<username>

    alt Evento ya procesado (idempotencia)
        MongoDB-->>CredService: Sí existe
        CredService-->>Consumer: Completado (sin acción)
        Note over Consumer: Evento duplicado ignorado
    else Evento nuevo
        MongoDB-->>CredService: No existe
        CredService->>CredService: Hashear contraseña temporal (BCrypt)
        CredService->>MongoDB: Insertar credencial<br/>(enabled=true)
        CredService->>MongoDB: Insertar processed_event
        CredService-->>Consumer: Completado
        Note over Consumer: Credencial creada exitosamente
    end
```

---

## 7. Endpoints REST

### 7.1 Autenticación

| Método | Ruta | Descripción | Autenticación |
|---|---|---|---|
| `POST` | `/api/auth/login` | Inicia sesión → devuelve access + refresh token | ❌ No requiere |
| `POST` | `/api/auth/refresh` | Refresca el access token usando el refresh token | ❌ No requiere |
| `POST` | `/api/auth/logout` | Cierra sesión → revoca el token en Redis y limpia cache | ✅ Bearer en header |
| `GET` | `/api/auth/validate` | Valida un access token y devuelve sus claims | ✅ Bearer en header |

### 7.2 Credenciales (Admin)

| Método | Ruta | Descripción | Autenticación |
|---|---|---|---|
| `POST` | `/api/auth/credentials` | Crea una nueva credencial | ✅ Admin |
| `GET` | `/api/auth/credentials` | Lista todas las credenciales | ✅ Admin |
| `GET` | `/api/auth/credentials/{id}` | Obtiene una credencial por ID | ✅ Admin |
| `PUT` | `/api/auth/credentials/{id}` | Actualiza credencial (password, roles, enabled) | ✅ Admin |
| `DELETE` | `/api/auth/credentials/{id}` | Elimina una credencial | ✅ Admin |

### 7.3 Ejemplos de Uso

```bash
# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "juan.perez@correo.com", "password": "TempPass123"}'

# Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "juan.perez@correo.com",
  "roles": ["ROLE_CUSTOMER"]
}

# Refresh
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzI1NiJ9..."}'

# Logout (el token va en el header, no en el body)
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Validar token
curl -X GET http://localhost:8081/api/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 8. Códigos de Error

| Código | HTTP | Causa |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | Username o password incorrectos, o credencial deshabilitada |
| `INVALID_TOKEN` | 401 | Header Authorization ausente, mal formado o firma inválida |
| `TOKEN_EXPIRED` | 401 | Access token expirado |
| `CREDENTIAL_NOT_FOUND` | 404 | Credencial no encontrada por ID |
| `CREDENTIAL_ALREADY_EXISTS` | 409 | Username duplicado |
| `VALIDATION_ERROR` | 400 | Campos del request inválidos |
| `INVALID_REQUEST` | 400 | Petición mal formada |
| `ROUTE_NOT_FOUND` | 404 | Ruta no registrada en el servicio |
| `METHOD_NOT_ALLOWED` | 405 | Método HTTP no permitido en la ruta |
| `INTERNAL_ERROR` | 500 | Error inesperado no controlado |

---

## 9. Modelo de Datos

### 9.1 Credencial (`credentials` en MongoDB)

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | String | ID único (MongoDB ObjectId) |
| `username` | String | Email en minúsculas (único, indexado) |
| `passwordHash` | String | Contraseña hasheada con BCrypt (cost 12) |
| `customerId` | String | ID del cliente asociado (indexado) |
| `email` | String | Email del usuario (indexado) |
| `roles` | List\<String\> | Roles: `ROLE_CUSTOMER`, `ROLE_OPERATOR`, `ROLE_ADMIN` |
| `enabled` | Boolean | `true` = activa, `false` = deshabilitada |
| `createdAt` | Instant | Fecha de creación |
| `updatedAt` | Instant | Última modificación |
| `lastLoginAt` | Instant | Último acceso exitoso |

> **Nota**: Los campos `failedAttempts` y `lockedUntil` no existen en el modelo actual. El servicio no implementa bloqueo por intentos fallidos.

### 9.2 Evento Procesado (`processed_events` en MongoDB)

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | String | ID único (MongoDB ObjectId) |
| `eventKey` | String | Clave de idempotencia única: `<topic>:<customerId>:<username>` |
| `topic` | String | Topic de Kafka origen |
| `entityId` | String | customerId del evento |
| `processedAt` | Instant | Fecha de procesamiento |

---

## 10. Redis Cache

| Clave | Contenido | TTL | Propósito |
|---|---|---|---|
| `yanki:auth:credential:{username}` | Credencial JSON | 15 minutos (configurable) | Cache de credenciales para evitar consultas a MongoDB |
| `yanki:auth:blacklist:{token}` | `1` | Tiempo restante del token | Lista negra de tokens revocados por logout |

---

## 11. Eventos Kafka

### 11.1 Consumidos (Inbound)

| Topic | Payload | Acción |
|---|---|---|
| `yanki.customer.registered.v1` | `CustomerRegisteredEvent` | Crear credencial automática con contraseña temporal |

El payload `CustomerRegisteredEvent` incluye:
- `customerId` — identificador del cliente
- `username` — nombre de usuario de la credencial
- `email` — correo electrónico del cliente
- `temporaryPassword` — contraseña temporal en claro
- `roles` — roles del cliente
- `customerType` — tipo de cliente (PERSONAL, CORPORATE)
- `status` — estado del cliente (ACTIVE, BLOCKED, DELETED)

### 11.2 Publicados (Outbound)

**NINGUNO** — auth-service no produce eventos de dominio.

---

## 12. Configuración

### 12.1 Variables de Entorno

| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8081` | Puerto HTTP |
| `MONGODB_URI` | **REQUERIDO** | URI de conexión MongoDB |
| `REDIS_HOST` | `localhost` | Host Redis |
| `REDIS_PORT` | `6379` | Puerto Redis |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Brokers Kafka |
| `KAFKA_GROUP_ID` | `auth-service` | Consumer group ID |
| `EUREKA_URL` | `http://discovery-service:8761/eureka/` | Service Discovery |
| `CONFIG_SERVER_URL` | `http://localhost:8888` | Config Server |
| `JWT_SECRET` | **REQUERIDO** | Clave HS256 (mínimo 32 caracteres) — **debe coincidir con api-gateway** |
| `JWT_ISSUER` | `yanki-platform` | Emisor del token — **debe coincidir con api-gateway** |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `1h` | Vigencia del access token |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `7d` | Vigencia del refresh token |
| `CREDENTIAL_CACHE_TTL` | `15m` | TTL de la cache de credenciales en Redis |
| `CREDENTIAL_CACHE_KEY_PREFIX` | `yanki:auth:credential:` | Prefijo de las claves de cache |

### 12.2 Roles Disponibles

| Rol | Descripción |
|---|---|
| `ROLE_CUSTOMER` | Cliente individual con acceso a sus propios recursos |
| `ROLE_ENTERPRISE` | Cliente empresarial con acceso a recursos corporativos |
| `ROLE_OPERATOR` | Operador del banco con acceso a gestión de clientes |
| `ROLE_ADMIN` | Administrador con acceso completo al sistema |

---

## 13. Cómo Ejecutarlo

### 13.1 Desarrollo Local

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17.0.21.0.3"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn spring-boot:run
```

### 13.2 Build y Pruebas

```powershell
mvn clean verify
```

### 13.3 Docker

```bash
docker build -t yanki/auth-service:1.0.0 .
docker run -p 8081:8081 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/yanki_auth \
  -e REDIS_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  -e JWT_SECRET=<32-chars-min> \
  -e JWT_ISSUER=yanki-platform \
  yanki/auth-service:1.0.0
```

### 13.4 Verificar Salud

```bash
curl http://localhost:8081/actuator/health
```

---

## 14. Estructura del Código

```
src/main/java/com/yanki/auth
├── AuthServiceApplication.java
├── domain
│   ├── event
│   │   ├── CustomerRegisteredEvent.java   # DTO del evento Kafka
│   │   └── Topics.java                    # Nombres de topics
│   ├── exception
│   │   └── BusinessException.java         # Excepciones de negocio
│   ├── model
│   │   ├── Credential.java                # Entidad de credencial
│   │   ├── CredentialCreateCommand.java   # Comando de creación
│   │   ├── CredentialUpdateCommand.java   # Comando de actualización
│   │   ├── IssuedTokens.java              # Tokens emitidos (login/refresh)
│   │   ├── LoginCommand.java              # Comando de login
│   │   ├── ProcessedEvent.java            # Evento procesado (idempotencia)
│   │   ├── RefreshCommand.java            # Comando de refresh
│   │   ├── TokenClaims.java               # Claims extraídos de un JWT
│   │   └── TokenValidationResult.java     # Resultado de validación
│   ├── repository
│   │   ├── CredentialRepository.java      # Repositorio reactivo MongoDB
│   │   └── ProcessedEventRepository.java  # Repositorio reactivo MongoDB
│   └── service
│       ├── AuthService.java               # Login, logout, refresh, validate
│       ├── CredentialCacheService.java    # Cache de credenciales en Redis
│       ├── CredentialService.java         # CRUD credenciales + onboarding
│       └── JwtService.java                # Emisión y parseo de JWT
├── infrastructure
│   ├── cache
│   │   └── TokenBlacklistService.java     # Almacena tokens revocados en Redis
│   ├── config
│   │   ├── CacheProperties.java           # Propiedades de cache
│   │   ├── JwtProperties.java             # Propiedades JWT
│   │   └── SecurityConfig.java            # Configuración de seguridad
│   ├── messaging
│   │   └── CredentialConsumer.java        # Consumidor Kafka de onboarding
│   └── persistence
│       └── ReactiveAdapter.java           # Adaptador reactivo MongoDB
└── interfaces
    └── rest
        ├── GlobalExceptionHandler.java    # Manejador global de errores
        ├── controller
        │   ├── AuthController.java        # Login, logout, refresh, validate
        │   └── CredentialController.java  # CRUD credenciales (admin)
        └── dto
            ├── ApiError.java              # DTO de error unificado
            ├── CredentialCreateRequest.java
            ├── CredentialResponse.java
            ├── CredentialUpdateRequest.java
            ├── LoginRequest.java
            ├── LogoutResponse.java        # Confirmación de logout exitoso
            ├── RefreshTokenRequest.java
            ├── TokenResponse.java
            └── TokenValidationResponse.java
```

---

## 15. Casos de Uso

### CU-01: Login exitoso

1. Cliente envía `POST /api/auth/login` con username y password
2. Servicio busca la credencial (primero en cache Redis, luego en MongoDB)
3. Se verifica que la credencial exista, esté habilitada y la contraseña coincida
4. Se actualiza el último acceso (`lastLoginAt`) en MongoDB y Redis
5. Se emiten dos tokens JWT: access (1h) y refresh (7 días)
6. Se devuelve `200` con ambos tokens, username y roles

### CU-02: Login con credenciales inválidas

1. Cliente envía `POST /api/auth/login` con username o password incorrectos
2. Servicio no encuentra la credencial o la contraseña no coincide
3. Se devuelve `401 INVALID_CREDENTIALS`

### CU-03: Logout con revocación inmediata

1. Cliente envía `POST /api/auth/logout` con `Authorization: Bearer <token>`
2. Servicio parsea el token para obtener el username y la fecha de expiración
3. Se calcula el tiempo restante de expiración
4. Se guarda el token en Redis (`yanki:auth:blacklist:<token>`) con TTL = tiempo restante
5. Se elimina la credencial de la cache de Redis
6. Se devuelve `200` con mensaje de confirmación
7. **Resultado:** Si el cliente intenta usar el mismo token, el api-gateway lo rechaza

### CU-04: Refresh token exitoso

1. El access token expiró (1 hora)
2. Cliente envía `POST /api/auth/refresh` con el refresh token
3. Servicio valida el refresh token y verifica que la credencial siga habilitada
4. Se emite un nuevo access token
5. Se devuelve `200` con el nuevo par de tokens

### CU-05: Onboarding automático por Kafka

1. `customers-service` publica `yanki.customer.registered.v1` en Kafka
2. `CredentialConsumer` recibe el evento
3. Se verifica idempotencia mediante `eventKey` en `processed_events`
4. Si es nuevo: se hashea la contraseña temporal (BCrypt) y se crea la credencial
5. Se registra el evento procesado para evitar duplicados
6. El cliente puede hacer login con su email y la contraseña temporal

### CU-06: Petición con token revocado

1. Cliente hizo logout (token en blacklist de Redis)
2. Cliente envía petición al api-gateway con el mismo token
3. El gateway valida la firma JWT (válida)
4. El gateway consulta Redis: token **sí** está en blacklist
5. El gateway responde `401 INVALID_TOKEN` — el token fue revocado

---

## 16. Notas Importantes

### 16.1 Sincronización con api-gateway

- `JWT_SECRET` y `JWT_ISSUER` **deben ser idénticos** en auth-service y api-gateway.
- Si no coinciden, el gateway rechazará todos los tokens emitidos por auth-service.

### 16.2 Token Blacklist

- Los tokens se almacenan en Redis con el tiempo restante de expiración como TTL.
- Cuando el TTL expira, la entrada se elimina automáticamente de Redis.
- No se almacenan tokens ya expirados (no tiene sentido blacklisterizar un token que el gateway ya rechazaría por expiración).

### 16.3 Idempotencia de Eventos

- La clave de idempotencia es `<topic>:<customerId>:<username>`.
- Se almacena en el campo `eventKey` de `processed_events` con índice único.
- Un evento duplicado se detecta y se ignora sin duplicar la credencial.

### 16.4 Contraseñas Temporales

- ⚠️ La contraseña temporal viaja en claro por Kafka.
- Se recomienda forzar un cambio de contraseña al primer login del cliente.

### 16.5 Sin bloqueo por intentos fallidos

- El modelo `Credential` **no incluye** los campos `failedAttempts` ni `lockedUntil`.
- El servicio no implementa rate limiting ni bloqueo temporal de cuenta.
- La seguridad se basa en BCrypt, el flag `enabled` y la blacklist de tokens.

---

## 17. Referencias

- `Proyecto General vf.md` — Parte III § JWT, Parte I § Onboarding
- `README.md` (raíz) — Arquitectura general de la plataforma
- `AGENTS.md` (raíz) — Reglas de oro de trazabilidad y arquitectura hexagonal
- `api-gateway/README.md` — Servicio de gateway y validación JWT
- `config-repo/application.yml` — Configuración centralizada
- `docs/EVENTOS.md` — Evento `customer.registered.v1`
- `init-mongo-parte1.js` — Credenciales de prueba (admin, operador)
- `CASOS_USO/` — Casos de uso relacionados con autenticación
- `system-prompt-auditoria-hexagonal-yanki.md` — Guía de auditoría hexagonal
