# Catálogo de Ecommerce

Aplicación con una API REST Spring Boot y un cliente web Angular. El backend expone el catálogo, permite guardar notas por producto, registra auditorías y protege sus rutas con tokens JWT.

## Inicio rápido

1. Desde la raíz del proyecto, copia `.env.example` a `.env`:

   ```powershell
   # Windows PowerShell
   Copy-Item .env.example .env
   ```

   ```sh
   # macOS / Linux
   cp .env.example .env
   ```

2. Genera un secreto JWT aleatorio de 32 bytes. En PowerShell:

   ```powershell
   $bytes = New-Object byte[] 32
   $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   [Convert]::ToBase64String($bytes)
   ```

   En macOS (con OpenSSL):

   ```sh
   openssl rand -base64 32
   ```

   Copia el resultado en `JWT_SECRET` dentro de `.env` y configura `POSTGRES_URL=jdbc:postgresql://postgres:5432/database`. No compartas ni publiques `.env`.

3. Levanta la aplicación desde la raíz del proyecto:

   ```sh
   docker compose -f docker-compose.yaml up --build
   ```

4. Abre **http://localhost:4200**. La API está en **http://localhost:8080**.

## Tecnologías

- **Frontend:** Angular 21; cliente web en desarrollo.
- **Backend:** Java 17, Spring Boot 3.5, Spring Security y Spring Data JPA.
- **Persistencia:** PostgreSQL.
- **Catálogo externo:** Fake Store API, consumida desde el backend.
- **Pruebas de cobertura:** JaCoCo aplica un mínimo global de 70% de cobertura de líneas durante `verify`.

## Arquitectura y comunicación

El backend sigue una arquitectura de puertos y adaptadores. Los controladores reciben HTTP, los casos de uso coordinan la lógica y los adaptadores conectan esa lógica con PostgreSQL o con Fake Store API.

```text
Navegador
  │
  ▼
Angular
  ├── AuthService: envía credenciales y conserva el access token en memoria
  └── Interceptor HTTP: agrega Authorization: Bearer <token>
  │ HTTP / JSON
  ▼
API Spring Boot
  ├── Controladores REST
  ├── Spring Security: valida el JWT y los roles
  ├── Aplicación y dominio: ejecutan los casos de uso y reglas
  └── Adaptadores: PostgreSQL y Fake Store API
```

### Inicio de sesión y autorización

1. Angular envía usuario y contraseña a `POST /api/auth/login`.
2. Spring Security valida las credenciales contra el usuario almacenado; las contraseñas se comparan con BCrypt.
3. Si son válidas, la API responde con `accessToken`, `tokenType` (`Bearer`) y `expiresIn`.
4. Angular mantiene el token en memoria y su interceptor lo agrega a las solicitudes protegidas como `Authorization: Bearer <accessToken>`.
5. Spring Security verifica el token y aplica los roles requeridos por cada ruta. La API no necesita mantener una sesión entre solicitudes.

La autenticación utiliza JWT Bearer y la validación de tokens de Spring Security OAuth2 Resource Server. El inicio de sesión es un endpoint propio: el proyecto no está configurado para redirigir a un proveedor de identidad OAuth externo.

## API REST

`POST /api/auth/login` y `GET /api/v1/test` son públicos. Las demás rutas requieren un token Bearer válido. `ADMIN` y `USER` indican los roles permitidos.

| Método y ruta | Acceso | Función |
|---|---|---|
| `POST /api/auth/login` | Público | Autentica al usuario y devuelve un JWT. |
| `GET /api/v1/test` | Público | Comprueba que la API responde (`Success`). |
| `GET /api/v1/products` | `ADMIN`, `USER` | Obtiene el catálogo externo con las notas locales asociadas. |
| `POST /api/v1/prodnotes` | `ADMIN` | Crea una nota para un producto. |
| `GET /api/v1/prodnotes` | `ADMIN`, `USER` | Lista las notas guardadas. |
| `GET /api/v1/auditlogs` | `ADMIN`, `USER` | Consulta los registros de auditoría. |

### Ejemplos

Iniciar sesión:

```sh
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin123","password":"Admin123!"}'
```

Usa el `accessToken` de la respuesta para consultar el catálogo:

```sh
curl http://localhost:8080/api/v1/products \
  -H 'Authorization: Bearer <accessToken>'
```

Crear una nota (requiere rol `ADMIN`):

```sh
curl -X POST http://localhost:8080/api/v1/prodnotes \
  -H 'Authorization: Bearer <accessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"extProdId":1,"note":"Buen producto","createdBy":"admin"}'
```

## Pruebas y cobertura del backend

Desde `backend`, ejecuta `.\mvnw.cmd verify` en Windows o `./mvnw verify` (`mvnw verify`) en macOS/Linux. JaCoCo genera el reporte en `backend/target/site/jacoco/index.html` y hace fallar la fase `verify` si la cobertura global de líneas queda por debajo del 70%.

## Cuenta de prueba

Usuario **`admin123`**, contraseña **`Admin123!`**, rol `ROLE_ADMIN`. Solo para pruebas locales. El seed se ejecuta al inicializar PostgreSQL por primera vez; los volúmenes existentes conservan sus datos.

#### Donato Angel Bautista Dionicio