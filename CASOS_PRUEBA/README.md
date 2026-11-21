# Casos de Prueba — auth-service

Este directorio concentra los casos de prueba específicos del microservicio `auth-service`, derivados de:

- `CASOS_PRUEBA/CASOS_PRUEBA_QA.md`
- `CASOS_USO/CU-10-Autenticacion-JWT.md`
- `auth-service/README.md`
- `auth-service/AGENTS.md`
- `README.md` raíz

## Objetivo

Permitir validar el comportamiento funcional del microservicio de forma aislada, cubriendo:

- happy path
- casos exitosos
- flujos alternativos
- errores esperados
- integración interna del servicio con mocks de sistemas externos

## Alcance

Estas pruebas son **solo para `auth-service`**.  
Por lo tanto, toda dependencia externa debe mockearse o simularse:

- **MongoDB** → mock de repositorios reactivos
- **Redis** → mock de cache, blacklist y rate limiting
- **Kafka** → mock del consumer input / payloads de eventos
- **Config Server** → mock/config local
- **Eureka** → deshabilitado o mockeado
- **api-gateway** → no se prueba directamente; solo se valida compatibilidad contractual
- **customers-service** → simulado mediante eventos Kafka mock

## Estructura de este directorio

- `README.md` → visión general
- `CP-052-login-exitoso.md`
- `CP-053-login-credenciales-invalidas.md`
- `CP-054-validacion-sin-jwt.md`
- `CP-055-validacion-jwt-invalido-o-expirado.md`
- `CP-AUTH-001-refresh-exitoso.md`
- `CP-AUTH-002-credencial-duplicada.md`
- `CP-AUTH-003-credencial-no-encontrada.md`
- `CP-AUTH-004-listado-credenciales.md`
- `CP-AUTH-005-actualizacion-credencial.md`
- `CP-AUTH-006-eliminacion-credencial.md`
- `CP-AUTH-007-onboarding-automatico.md`
- `CP-AUTH-008-idempotencia-evento-duplicado.md`
- `CP-AUTH-009-cuenta-bloqueada.md`
- `CP-AUTH-010-cuenta-deshabilitada.md`
- `CP-AUTH-011-request-invalido.md`
- `CP-AUTH-012-error-interno-controlado.md`
- `MOCKS.md`

## Criterios de diseño de prueba

Cada caso debe documentar:

1. identificador
2. objetivo
3. origen documental
4. precondiciones
5. mocks requeridos
6. datos de entrada
7. pasos
8. resultado esperado
9. respuesta HTTP esperada
10. observaciones de implementación

## Consideraciones importantes

1. El código actual expone:
   - `POST /api/auth/login`
   - `POST /api/auth/refresh`
   - `GET /api/auth/validate`
   - `POST /api/auth/credentials`
   - `GET /api/auth/credentials`
   - `GET /api/auth/credentials/{id}`
   - `PUT /api/auth/credentials/{id}`
   - `DELETE /api/auth/credentials/{id}`

2. `logout` está documentado a nivel funcional en CU-10, pero no fue observado en el controller actual.

3. La validación JWT en `auth-service` debe probarse a nivel de contrato del endpoint y lógica del servicio, no como prueba de gateway.

4. La creación automática de credenciales debe probarse simulando el consumo del evento:
   - `yanki.customer.registered.v1`

## Tipos de mocks sugeridos

- **Mockito** para servicios, repositorios y colaboraciones internas
- **StepVerifier** para publishers reactivos
- **fixtures JSON** para requests/responses y eventos Kafka
- **dobles de prueba** para Redis blacklist/rate limit

## Resultado esperado

Este directorio debe permitir a un agente o desarrollador ejecutar y/o implementar la suite mínima del microservicio de manera aislada, sin depender de infraestructura real compartida.
