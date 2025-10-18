# Frontend de StreamHub

## � ACTUALIZACIÓN IMPORTANTE - Versión 2.0 con JWT

**⚡ Esta versión incluye autenticación JWT con AWS Cognito**

Todas las peticiones ahora requieren un token JWT válido. Ver sección "Autenticación JWT" más abajo.

## �📋 Descripción

Interfaz web moderna y responsiva para interactuar con el API REST de StreamHub. Diseñada con una paleta de colores tierra y café, proporciona una experiencia de usuario intuitiva para gestionar usuarios, streams y posts.

## 🎨 Características de Diseño

### Paleta de Colores

- **Primarios**: Tonos cafés y tierra (Saddle Brown, Sienna, Peru)
- **Secundarios**: Beige y Burlywood para fondos
- **Acentos**: Degradados suaves para botones y encabezados
- **Tema**: Natural, cálido y profesional

### Características Visuales

- ✨ Diseño moderno con sombras suaves y bordes redondeados
- 🎭 Animaciones fluidas y transiciones suaves
- 📱 Totalmente responsivo (desktop, tablet, móvil)
- 🌈 Efectos hover interactivos
- 💫 Degradados modernos en elementos clave
- 🔔 Sistema de notificaciones toast

## 🚀 Uso

### 🔐 1. Configurar Autenticación JWT (NUEVO)

**IMPORTANTE:** Ahora necesitas autenticarte antes de usar la aplicación.

**Método Rápido (Recomendado):**

1. Abre `login-helper.html` en tu navegador
2. Ingresa credenciales:
   - Usuario: `testuser`
   - Contraseña: `Password123!`
3. Click en "Iniciar Sesión"
4. Abre `index.html`

**Método Alternativo (Consola):**

1. Abre `index.html`
2. Presiona F12 → Consola
3. Ejecuta:

```javascript
fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    username: "testuser",
    password: "Password123!",
  }),
})
  .then((r) => r.json())
  .then((tokens) => {
    localStorage.setItem("idToken", tokens.idToken);
    location.reload();
  });
```

📚 **Documentación Completa:** Ver `JWT_SETUP.md`

### 2. Iniciar el Backend

Los 3 microservicios deben estar corriendo:

```powershell
# Desde la raíz del proyecto
.\iniciar-servicios-cognito.ps1
```

O manualmente:

```bash
# Terminal 1 - Users (puerto 8080)
cd users
mvn spring-boot:run

# Terminal 2 - Posts (puerto 8081)
cd posts
mvn spring-boot:run

# Terminal 3 - Streams (puerto 8082)
cd streams
mvn spring-boot:run
```

### 3. Abrir el Frontend

Simplemente abre el archivo `index.html` en tu navegador:

- **Opción 1**: Haz doble clic en `index.html`
- **Opción 2**: Abre con un servidor local (recomendado):

  ```bash
  # Con Python 3
  python -m http.server 8000

  # Con Node.js (http-server)
  npx http-server -p 8000
  ```

  Luego visita: `http://localhost:8000`

### 4. Navegar por la Aplicación

La aplicación tiene 4 secciones principales:

#### 🏠 Inicio

- Dashboard con estadísticas generales
- Streams recientes
- Posts recientes
- Vista general del sistema

#### 📋 Streams

- Ver todos los streams
- Crear nuevos streams
- Filtrar por usuario
- Buscar streams
- Ver detalles con posts
- Eliminar streams

#### 👥 Usuarios

- Ver todos los usuarios
- Crear nuevos usuarios
- Buscar usuarios
- Ver streams y posts de cada usuario
- Eliminar usuarios

#### 👤 Mi Perfil

- Seleccionar un usuario para ver su perfil completo
- Ver estadísticas del usuario
- Ver todos los streams creados
- Ver todos los posts publicados
- Gestionar contenido propio

## 📡 Endpoints Utilizados

El frontend consume todos los endpoints del API REST:

### Usuarios

- `GET /api/users` - Obtener todos los usuarios
- `GET /api/users/{id}` - Obtener usuario por ID
- `POST /api/users` - Crear usuario
- `DELETE /api/users/{id}` - Eliminar usuario

### Streams

- `GET /api/streams` - Obtener todos los streams
- `GET /api/streams/{id}` - Obtener stream por ID
- `GET /api/streams/user/{userId}` - Streams de un usuario
- `GET /api/streams/{id}/posts` - Posts de un stream
- `GET /api/streams/{id}/info` - Info del stream con contador
- `POST /api/streams` - Crear stream
- `DELETE /api/streams/{id}` - Eliminar stream

### Posts

- `GET /api/posts` - Obtener todos los posts
- `GET /api/posts/{id}` - Obtener post por ID
- `GET /api/posts/user/{userId}` - Posts de un usuario
- `GET /api/posts/stream/{streamId}` - Posts de un stream
- `POST /api/posts` - Crear post
- `DELETE /api/posts/{id}` - Eliminar post

## 🎯 Funcionalidades Principales

### Gestión de Usuarios

- ✅ Crear usuarios con username, email, nombre completo y biografía
- ✅ Visualizar lista de usuarios con avatares generados
- ✅ Buscar usuarios en tiempo real
- ✅ Ver estadísticas de cada usuario (streams y posts)
- ✅ Eliminar usuarios (en cascada: elimina sus streams y posts)

### Gestión de Streams

- ✅ Crear streams con título, descripción y creador
- ✅ Ver lista de streams con contador de posts
- ✅ Filtrar streams por usuario
- ✅ Buscar streams por título o descripción
- ✅ Ver detalles completos de un stream
- ✅ Ver todos los posts dentro de un stream
- ✅ Agregar posts directamente a un stream
- ✅ Eliminar streams (en cascada: elimina sus posts)

### Gestión de Posts

- ✅ Crear posts de máximo 140 caracteres
- ✅ Contador de caracteres en tiempo real
- ✅ Seleccionar usuario y stream para el post
- ✅ Ver posts con información del autor y stream
- ✅ Formateo de fechas relativas (hace X minutos/horas/días)
- ✅ Eliminar posts individuales

### Características UX

- ✅ Sistema de notificaciones toast (éxito, error, advertencia, info)
- ✅ Indicador de carga global
- ✅ Modales elegantes para formularios
- ✅ Validaciones en tiempo real
- ✅ Confirmaciones antes de eliminar
- ✅ Actualización automática de datos
- ✅ Navegación fluida entre secciones
- ✅ Estados vacíos informativos

## 📱 Diseño Responsivo

El diseño se adapta a diferentes tamaños de pantalla:

### Desktop (> 1024px)

- Grid de múltiples columnas
- Navegación horizontal completa
- Visualización óptima de tarjetas

### Tablet (768px - 1024px)

- Grid adaptado a 2 columnas
- Navegación ajustada
- Tarjetas más grandes

### Mobile (< 768px)

- Grid de 1 columna
- Navegación compacta (solo iconos)
- Modales en pantalla completa
- Filtros apilados verticalmente

## 🎨 Componentes Principales

### Tarjetas de Stream

- Título y descripción
- Información del creador
- Contador de posts
- Fecha de creación
- Acciones rápidas (ver, nuevo post, eliminar)

### Tarjetas de Usuario

- Avatar con iniciales
- Username y email
- Nombre completo y biografía
- Fecha de registro
- Estadísticas (streams y posts)
- Acciones rápidas

### Tarjetas de Post

- Avatar del autor
- Nombre del autor
- Contenido del post (140 caracteres)
- Etiqueta del stream
- Fecha relativa
- Acciones (eliminar)

### Modales

- Crear Usuario
- Crear Stream
- Crear Post
- Detalle de Stream (con lista de posts)

## 🔧 Configuración

### Cambiar la URL del Backend

Si el backend está en otra dirección, edita `app.js`:

```javascript
const API_BASE_URL = "http://tu-servidor:puerto/api";
```

### Personalizar Colores

Los colores se definen en variables CSS en `styles.css`:

```css
:root {
  --color-primary: #8b4513; /* Color principal */
  --color-primary-dark: #654321; /* Versión oscura */
  --color-primary-light: #a0522d; /* Versión clara */
  /* ... más colores */
}
```

## 📦 Estructura de Archivos

```
frontendFiles/
├── index.html          # Estructura HTML principal
├── styles.css          # Estilos CSS con paleta tierra/café
├── app.js              # Lógica JavaScript y llamadas al API
└── README.md           # Esta documentación
```

## 🌟 Características Técnicas

### HTML

- Estructura semántica
- Accesibilidad mejorada
- Meta tags para responsive

### CSS

- Variables CSS para temas
- Grid y Flexbox modernos
- Animaciones y transiciones
- Media queries para responsive
- Efectos hover y estados

### JavaScript

- Vanilla JS (sin frameworks)
- Async/Await para APIs
- Manejo de errores robusto
- Event delegation
- Actualización dinámica del DOM
- Formateo de fechas
- Validaciones en cliente

## 🔐 Validaciones

### Frontend

- Campos requeridos en formularios
- Límite de 140 caracteres en posts
- Formato de email válido
- Confirmación antes de eliminar

### Backend

- Validaciones adicionales en el servidor
- Manejo de errores con mensajes claros
- Integridad referencial

## 🐛 Solución de Problemas

### ❌ "No está autenticado" (NUEVO)

**Causa:** No hay token JWT guardado  
**Solución:**

1. Abre `login-helper.html`
2. Inicia sesión con testuser/Password123!
3. Recarga `index.html`

O ejecuta en la consola:

```javascript
localStorage.setItem("idToken", "TU_TOKEN_AQUI");
location.reload();
```

### ❌ "Session expired" (NUEVO)

**Causa:** El token JWT expiró (duración: 1 hora)  
**Solución:** Haz login de nuevo usando `login-helper.html`

### ❌ Error 401/403 en las peticiones (NUEVO)

**Causa:** Token inválido o expirado  
**Solución:**

1. Limpia el token: `localStorage.clear()`
2. Haz login de nuevo
3. Verifica que Cognito esté configurado correctamente

### El frontend no carga datos

1. Verifica que el backend esté corriendo en `http://localhost:8080`
2. Revisa la consola del navegador (F12) para errores
3. Verifica que el CORS esté habilitado en el backend
4. **NUEVO:** Verifica que tengas un token JWT válido guardado

### Error de CORS

El backend ya tiene CORS habilitado con `@CrossOrigin(origins = "*")` en todos los controladores.

### Los estilos no se cargan

1. Asegúrate de que `styles.css` esté en el mismo directorio que `index.html`
2. Limpia la caché del navegador (Ctrl+F5)
3. Verifica que el archivo CSS no tenga errores de sintaxis

### Las notificaciones no aparecen

1. Revisa la consola del navegador
2. Verifica que Font Awesome esté cargando correctamente
3. Asegúrate de que JavaScript esté habilitado

---

## 🔐 Autenticación JWT (NUEVO EN v2.0)

### ¿Qué cambió?

Esta versión incluye autenticación completa con AWS Cognito y JWT tokens.

**Todas las peticiones ahora requieren:**

```http
Authorization: Bearer <jwt_token>
```

### Funciones de Autenticación Disponibles

```javascript
// Cargar token desde localStorage
loadAuthToken();

// Guardar nuevo token
saveAuthToken("tu_token_aqui");

// Limpiar token (logout)
clearAuthToken();

// Verificar autenticación
console.log(isAuthenticated); // true/false
console.log(authToken); // token actual o null
```

### Verificar Token en Consola

```javascript
// Ver token guardado
console.log("Token:", localStorage.getItem("idToken"));

// Decodificar token
const token = localStorage.getItem("idToken");
const payload = JSON.parse(atob(token.split(".")[1]));
console.log("Usuario:", payload["cognito:username"]);
console.log("Email:", payload.email);
console.log("Expira:", new Date(payload.exp * 1000));
```

### Testing Automático

Ejecuta desde la raíz del proyecto:

```powershell
.\test-jwt-frontend.ps1
```

Este script verifica automáticamente:

- ✅ Servicios backend corriendo
- ✅ Obtención de token JWT
- ✅ Peticiones autenticadas funcionando
- ✅ Protección de endpoints activa

### Documentación Completa

- **`JWT_SETUP.md`** - Guía completa de JWT
- **`../INICIO_RAPIDO_JWT.md`** - Inicio rápido
- **`../AWS_COGNITO_INTEGRATION.md`** - Configuración Cognito
- **`../RESUMEN_CAMBIOS_JWT_FRONTEND.md`** - Detalles técnicos

### Flujo de Autenticación

```
Usuario → login-helper.html → Cognito
     ↓
  Token JWT guardado en localStorage
     ↓
index.html → loadAuthToken() al iniciar
     ↓
Todas las peticiones incluyen: Authorization: Bearer <token>
     ↓
Backend valida token con Cognito JWKS
     ↓
✅ Respuesta exitosa  |  ❌ 401/403 → Relogin
```

---

## 📝 Notas Importantes

- ⚠️ **Validación de datos**: El frontend valida, pero el backend es la fuente de verdad
- 🔄 **Actualización**: Los datos se recargan después de cada operación CRUD
- 🗑️ **Eliminación en cascada**: Eliminar un usuario o stream elimina sus dependencias
- 📊 **Tiempo real**: Las estadísticas se actualizan automáticamente
- 🎨 **Personalización**: Puedes modificar fácilmente colores y estilos en `styles.css`

## 🚀 Mejoras Futuras

- [ ] Paginación para grandes cantidades de datos
- [ ] Búsqueda avanzada con múltiples filtros
- [ ] Edición de usuarios y streams
- [ ] Likes y comentarios en posts
- [ ] Modo oscuro
- [ ] Internacionalización (i18n)
- [ ] Autenticación y autorización
- [ ] WebSockets para actualizaciones en tiempo real
- [ ] Exportación de datos
- [ ] Temas personalizables

## 📄 Licencia

Este frontend forma parte del proyecto Taller7 - AREP.

---

**Creado con ❤️ usando HTML, CSS y JavaScript Vanilla**
