# CP-052 — Login exitoso y emisión de JWT

## 1. Identificación
- **ID**: CP-052
- **Nombre**: Autenticación válida y emisión de JWT
- **Tipo**: Seguridad / API
- **Prioridad**: Alta
- **Estado**: Listo para ejecutar
- **Microservicio**: `auth-service`

## 2. Objetivo
Validar que el endpoint de login autentica correctamente a un usuario válido y retorna tokens JWT.

## 3. Origen documental
- `../../docs/CASOS_PRUEBA`
- `../../docs/CASOS_USO`
- `auth-service/README.md`

## 4. Precondiciones
- Existe una credencial habilitada.
- La contraseña almacenada está hasheada con BCrypt.
- El usuario no está bloqueado.
- El usuario no está deshabilitado.

## 5. Mocks requeridos
- `CredentialRepository`
- `JwtService`
- servicio/cache de rate limiting
- servicio/cache de credenciales

## 6. Datos de entrada
```json
{
  "username": "cliente@yanki.com",
  "password": "TempPass123"
}
```

## 7. Pasos
1. Mockear búsqueda exitosa de la credencial.
2. Mockear comparación exitosa de password.
3. Mockear emisión de access token y refresh token.
4. Invocar `POST /api/auth/login`.
5. Verificar respuesta.

## 8. Resultado esperado
- Login exitoso.
- Se emiten `accessToken` y `refreshToken`.
- Se restablecen intentos fallidos.
- Se actualiza `lastLoginAt`.

## 9. Respuesta HTTP esperada
- **HTTP**: `200 OK`

Ejemplo esperado:
```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

## 10. Observaciones de implementación
- Validar con `WebTestClient` en controller test.
- Validar flujo reactivo con `StepVerifier` en service test.
