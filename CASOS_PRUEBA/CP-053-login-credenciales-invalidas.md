# CP-053 — Rechazo de autenticación con credenciales inválidas

## 1. Identificación
- **ID**: CP-053
- **Nombre**: Rechazo de autenticación con credenciales inválidas
- **Tipo**: Seguridad / Error
- **Prioridad**: Alta
- **Estado**: Listo para ejecutar
- **Microservicio**: `auth-service`

## 2. Objetivo
Validar que el endpoint de login rechaza solicitudes con credenciales inválidas y retorna el error adecuado.

## 3. Origen documental
- `CASOS_PRUEBA/CASOS_PRUEBA_QA.md`
- `CASOS_USO/CU-10-Autenticacion-JWT.md`
- `auth-service/README.md`

## 4. Precondiciones
- El usuario no existe o la contraseña es incorrecta.

## 5. Mocks requeridos
- `CredentialRepository`
- `JwtService`

## 6. Datos de entrada
```json
{
  "username": "cliente@yanki.com",
  "password": "ClaveIncorrecta"
}
```

## 7. Pasos
1. Mockear búsqueda de credencial como inexistente o con password inválido.
2. Invocar `POST /api/auth/login`.
3. Verificar respuesta de error.

## 8. Resultado esperado
- Login rechazado.
- El contador de intentos fallidos se incrementa si el usuario existe.
- No se emite token.

## 9. Respuesta HTTP esperada
- **HTTP**: `401 Unauthorized`
- **Body**:
```json
{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized",
  "code": "INVALID_CREDENTIALS",
  "message": "Username/password incorrectos",
  "path": "/api/auth/login"
}
```

## 10. Observaciones de implementación
- En caso de usuario inexistente, debe responder igual que si la credencial existe pero el password es incorrecto (no revelar existencia del usuario).
