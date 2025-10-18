# 🔐 Configuración del JWT Token en el Frontend

## 📋 Resumen

El frontend ahora está configurado para enviar automáticamente el JWT token en el header `Authorization` de todas las peticiones HTTP a los microservicios.

## 🚀 Cómo Obtener y Configurar el Token

### Método 1: Login con Credenciales (Recomendado para Testing)

1. **Abre la consola del navegador** (F12)

2. **Ejecuta el siguiente código** para hacer login:

```javascript
// Login con usuario y contraseña
const loginResponse = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    username: "testuser",
    password: "Password123!",
  }),
});

const tokens = await loginResponse.json();
console.log("Tokens recibidos:", tokens);

// Guardar el token automáticamente
localStorage.setItem("idToken", tokens.idToken);
localStorage.setItem("accessToken", tokens.accessToken);
localStorage.setItem("refreshToken", tokens.refreshToken);

// Recargar la página para que el frontend use el token
location.reload();
```

### Método 2: Configuración Manual del Token

Si ya tienes un JWT token (por ejemplo, de Postman o curl):

```javascript
// En la consola del navegador (F12)
localStorage.setItem("idToken", "TU_JWT_TOKEN_AQUI");

// Recargar la página
location.reload();
```

### Método 3: Verificar Token Actual

```javascript
// Ver el token actual
console.log("Token actual:", localStorage.getItem("idToken"));

// Verificar si está autenticado
console.log("Autenticado:", localStorage.getItem("idToken") !== null);
```

## 🔍 Cómo Verificar que el Token se está Enviando

### Opción 1: Developer Tools (Network Tab)

1. Abre **DevTools** (F12)
2. Ve a la pestaña **Network**
3. Recarga la página
4. Click en cualquier request (ej: `users`, `streams`, `posts`)
5. En la sección **Request Headers**, deberías ver:
   ```
   Authorization: Bearer eyJraWQiOiJ...
   ```

### Opción 2: Consola del Navegador

```javascript
// Ejecuta esto en la consola para ver qué headers se están enviando
fetch("http://localhost:8080/api/users", {
  headers: {
    Authorization: `Bearer ${localStorage.getItem("idToken")}`,
  },
})
  .then((r) => r.json())
  .then((users) => console.log("Usuarios:", users))
  .catch((err) => console.error("Error:", err));
```

## ✅ Flujo Completo de Autenticación

### 1. Crear Usuario en Cognito

```bash
# Usando AWS CLI
aws cognito-idp admin-create-user \
  --user-pool-id us-east-1_XXXXXXXXX \
  --username testuser \
  --user-attributes Name=email,Value=test@example.com \
  --temporary-password TempPass123! \
  --message-action SUPPRESS

# Establecer contraseña permanente
aws cognito-idp admin-set-user-password \
  --user-pool-id us-east-1_XXXXXXXXX \
  --username testuser \
  --password Password123! \
  --permanent
```

### 2. Obtener Token (En la Consola del Navegador)

```javascript
// Login
const loginResponse = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    username: "testuser",
    password: "Password123!",
  }),
});

const tokens = await loginResponse.json();

// Guardar token
localStorage.setItem("idToken", tokens.idToken);

// Recargar
location.reload();
```

### 3. Usar la Aplicación

Ahora todas las peticiones incluirán automáticamente el header:

```
Authorization: Bearer <tu_jwt_token>
```

## 🛠️ Funciones Disponibles en el Frontend

### Gestión de Tokens

```javascript
// Cargar token desde localStorage
loadAuthToken();

// Guardar nuevo token
saveAuthToken("nuevo_token_jwt");

// Limpiar token (logout)
clearAuthToken();

// Verificar autenticación
console.log(isAuthenticated); // true/false
console.log(authToken); // El token actual o null
```

### Hacer Peticiones Autenticadas

Todas las funciones de API ahora usan automáticamente el token:

```javascript
// Estas funciones ya incluyen el JWT en el header
await loadUsers();
await createUser({...});
await loadStreams();
await createStream({...});
await loadPosts();
await createPost({...});
// etc.
```

### Función Auxiliar: `authenticatedFetch`

También puedes usar directamente la función auxiliar para peticiones custom:

```javascript
// Ejemplo de uso personalizado
const response = await authenticatedFetch("http://localhost:8080/api/users", {
  method: "GET",
});
const users = await response.json();
```

## 🚨 Manejo de Errores

### Token Expirado o Inválido

Si el token expira o es inválido, el frontend:

1. **Detecta automáticamente** el error 401/403
2. **Limpia el token** del localStorage
3. **Muestra un mensaje** al usuario: "Sesión expirada. Por favor, inicie sesión nuevamente."
4. **Lanza una excepción** que puedes capturar

### Ejemplo de Manejo de Errores

```javascript
try {
  await loadUsers();
} catch (error) {
  if (error.message.includes("Session expired")) {
    console.log("Token expirado, necesitas hacer login de nuevo");
    // Aquí podrías redirigir a una página de login
  } else {
    console.error("Otro error:", error);
  }
}
```

## 📝 Estructura del JWT Token

Tu JWT token tiene la siguiente estructura:

```javascript
// Header
{
  "kid": "...",
  "alg": "RS256"
}

// Payload (Claims)
{
  "sub": "abc-123-def-456",
  "cognito:username": "testuser",
  "email": "test@example.com",
  "email_verified": true,
  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX",
  "origin_jti": "...",
  "aud": "...",
  "token_use": "id",
  "auth_time": 1234567890,
  "exp": 1234571490,  // Expira en 1 hora
  "iat": 1234567890
}
```

Puedes decodificar tu token en [jwt.io](https://jwt.io/) para ver su contenido.

## 🔄 Refresh de Tokens

Cuando el token expire (después de 1 hora), debes obtener uno nuevo:

```javascript
// Opción 1: Login de nuevo
const loginResponse = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    username: "testuser",
    password: "Password123!",
  }),
});
const tokens = await loginResponse.json();
localStorage.setItem("idToken", tokens.idToken);

// Opción 2: Usar refresh token (si está implementado)
const refreshToken = localStorage.getItem("refreshToken");
// ... llamar a endpoint de refresh
```

## 📊 Testing Completo

### Script de Testing en Consola

```javascript
console.log("=== JWT Frontend Testing ===");

// 1. Verificar token actual
console.log(
  "1. Token actual:",
  localStorage.getItem("idToken") ? "Presente ✓" : "Ausente ✗"
);

// 2. Login (si no hay token)
if (!localStorage.getItem("idToken")) {
  console.log("2. Haciendo login...");
  const loginResponse = await fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: "testuser",
      password: "Password123!",
    }),
  });

  if (loginResponse.ok) {
    const tokens = await loginResponse.json();
    localStorage.setItem("idToken", tokens.idToken);
    console.log("   Login exitoso ✓");
  } else {
    console.error("   Login fallido ✗");
  }
}

// 3. Test de peticiones
console.log("3. Testeando peticiones...");

try {
  const usersResponse = await fetch("http://localhost:8080/api/users", {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("idToken")}`,
    },
  });

  if (usersResponse.ok) {
    const users = await usersResponse.json();
    console.log(`   GET /users exitoso ✓ (${users.length} usuarios)`);
  } else {
    console.error(`   GET /users falló ✗ (${usersResponse.status})`);
  }

  const streamsResponse = await fetch("http://localhost:8082/api/streams", {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("idToken")}`,
    },
  });

  if (streamsResponse.ok) {
    const streams = await streamsResponse.json();
    console.log(`   GET /streams exitoso ✓ (${streams.length} streams)`);
  } else {
    console.error(`   GET /streams falló ✗ (${streamsResponse.status})`);
  }

  const postsResponse = await fetch("http://localhost:8081/api/posts", {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("idToken")}`,
    },
  });

  if (postsResponse.ok) {
    const posts = await postsResponse.json();
    console.log(`   GET /posts exitoso ✓ (${posts.length} posts)`);
  } else {
    console.error(`   GET /posts falló ✗ (${postsResponse.status})`);
  }

  console.log("\n✅ Testing completo!");
} catch (error) {
  console.error("❌ Error en testing:", error);
}
```

## 🎯 Checklist de Verificación

- [ ] JWT token guardado en localStorage con key `idToken`
- [ ] Peticiones incluyen header `Authorization: Bearer <token>`
- [ ] Respuestas exitosas (200) de los 3 microservicios
- [ ] Manejo de errores 401/403 funciona correctamente
- [ ] Token se mantiene después de recargar la página
- [ ] Logout limpia correctamente el token

## 📚 Próximos Pasos

1. **Implementar UI de Login**: Crear formulario visual en lugar de usar consola
2. **Auto-refresh**: Implementar renovación automática de tokens antes de expirar
3. **Session persistence**: Mantener sesión activa mientras el usuario usa la app
4. **Logout button**: Agregar botón visible para cerrar sesión

Ver `FRONTEND_COGNITO_INTEGRATION.md` para la implementación completa de UI.

## 🆘 Troubleshooting

### "No está autenticado"

- **Causa**: No hay token en localStorage
- **Solución**: Ejecuta el login en la consola y recarga

### "Session expired"

- **Causa**: Token expiró (1 hora después de creado)
- **Solución**: Haz login de nuevo

### "CORS error"

- **Causa**: El backend no tiene tu origen configurado
- **Solución**: Verifica que los 3 microservicios tengan CORS habilitado

### "403 Forbidden"

- **Causa**: Token inválido o para diferente User Pool
- **Solución**: Verifica que el token sea del User Pool correcto

### No se envía el header Authorization

- **Causa**: Función no usa `authenticatedFetch`
- **Solución**: Todas las funciones de API ya están actualizadas, recarga la página

## 📞 Contacto

Para más información, consulta:

- `AWS_COGNITO_INTEGRATION.md` - Configuración de Cognito
- `FRONTEND_COGNITO_INTEGRATION.md` - Integración completa del frontend
- `RESUMEN_COGNITO.md` - Resumen de implementación
