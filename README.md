# Backend AutoFacil

Backend de **AutoFacil Automotora**, una tienda web de vehículos nuevos y usados: 4 microservicios **Spring Boot 4** (Java 21) con **MySQL 8**, seguridad con **JWT y roles**, documentación en **Swagger** y despliegue con **Docker Compose en AWS EC2**.

Proyecto de **DSY1104 Desarrollo Fullstack II** (Duoc UC).
**Integrantes:** Bastián Espejo · Felipe Vilches · Tomás Acuña
**Frontend (React + Vite):** [AutoFacil-Automotora](https://github.com/xxxbmbmbm/AutoFacil-Automotora)

## Microservicios

| Microservicio | Puerto | Base de datos | Responsabilidad |
| --- | --- | --- | --- |
| `ms-usuarios` | 8081 | `autofacil_usuarios` | Registro, inicio de sesión con JWT, renovación y cierre de sesión, administración de usuarios |
| `ms-vehiculos` | 8082 | `autofacil_vehiculos` | Catálogo de vehículos y categorías |
| `ms-ventas` | 8083 | `autofacil_ventas` | Boletas (órdenes de compra) y su detalle |
| `ms-contacto` | 8084 | `autofacil_contacto` | Formulario de contacto y bandeja de notificaciones del administrador |

```text
Frontend (React)
   │  fetch + Authorization: Bearer <JWT>
   ├──► ms-usuarios  :8081 ──► MySQL
   ├──► ms-vehiculos :8082 ──► MySQL
   ├──► ms-ventas    :8083 ──► MySQL
   │        └──► ms-vehiculos   (consulta disponibilidad y marca VENDIDO con un token de rol SERVICIO)
   └──► ms-contacto  :8084 ──► MySQL
```

Cada microservicio es independiente, tiene su propia base de datos y la misma estructura en capas:

```text
src/main/java/cl/autofacil/<servicio>/
├── modelo/         Entidades JPA (tablas)
├── repositorio/    Interfaces Spring Data JPA
├── dto/            Datos de entrada (…Solicitud, con validaciones) y de salida (…Respuesta)
├── servicio/       Lógica de negocio
├── controlador/    Endpoints REST /api/v1/...
├── seguridad/      Filtro JWT, Spring Security y CORS
├── configuracion/  Swagger y datos iniciales
└── excepcion/      Errores de negocio y manejador global (respuestas JSON en español)
```

## Modelo de datos

`USUARIO ┄► BOLETA ─► DETALLE_BOLETA ◄─ PRODUCTO (vehículo) ◄─ CATEGORIA`

| Tabla | Microservicio | Detalle |
| --- | --- | --- |
| `usuario`, `token_renovacion` | ms-usuarios | Cuentas con rol y contraseña cifrada; tokens de renovación guardados como hash |
| `categoria`, `vehiculo` | ms-vehiculos | Cada vehículo pertenece a una categoría y está DISPONIBLE o VENDIDO |
| `boleta`, `detalle_boleta` | ms-ventas | La boleta guarda el `usuario_id`; el detalle guarda el `vehiculo_id` con una copia del precio pagado |
| `solicitud_contacto` | ms-contacto | Mensajes del formulario con estado, canal de respuesta y notas internas |

Las relaciones entre microservicios distintos (usuario → boleta, vehículo → detalle) se guardan como identificadores, sin clave foránea, porque cada servicio es dueño de su base de datos. Las tablas las crea Hibernate a partir de las entidades.

## Reglas de negocio principales

- El registro público siempre crea cuentas de **cliente**; no se repiten correos.
- Una compra valida que cada vehículo esté disponible, calcula el total en el servidor y marca los vehículos como vendidos. Si algo falla a mitad de camino, la compra se revierte.
- Anular una boleta devuelve los vehículos a la tienda; solo se eliminan boletas anuladas.
- No se puede eliminar una categoría con vehículos, y el precio anterior de una oferta debe ser mayor al actual.
- Una solicitud de contacto marcada como respondida debe indicar el canal (WhatsApp, correo o llamada).
- El administrador no puede eliminarse ni quitarse su propio rol.

## Seguridad y roles

- `POST /api/v1/auth/login` entrega un **token de acceso JWT** (HS256, 15 minutos) y un **token de renovación** (7 días).
- Cada microservicio valida el JWT con el mismo secreto (`JWT_SECRETO`) sin consultar a ms-usuarios.
- El token de renovación se guarda solo como hash SHA-256, se rota en cada uso y se revoca al cerrar sesión.
- Contraseñas cifradas con **BCrypt**.
- Sin token la API responde **401**; con un rol sin permiso, **403**.

| Rol | Permisos |
| --- | --- |
| `ADMIN` | Acceso total: usuarios, vehículos, categorías, boletas y bandeja de contacto |
| `VENDEDOR` | Ver vehículos y su detalle; ver boletas y su detalle |
| `CLIENTE` | Tienda: comprar, ver sus boletas y enviar mensajes de contacto |
| `SERVICIO` | Interno: ms-ventas cambia el estado de un vehículo al vender o anular |

## Endpoints (prefijo `/api/v1`)

| Servicio | Endpoints |
| --- | --- |
| ms-usuarios | `POST /auth/registro`, `POST /auth/login`, `POST /auth/renovar`, `POST /auth/cerrar-sesion`, `GET /auth/perfil`, CRUD `/usuarios` |
| ms-vehiculos | `GET /vehiculos` (filtros `marca`, `categoriaId`, `estado`), `GET /vehiculos/marcas`, CRUD `/vehiculos`, `PATCH /vehiculos/{id}/estado`, CRUD `/categorias` |
| ms-ventas | `POST /boletas`, `GET /boletas`, `GET /boletas/mias`, `GET /boletas/{id}`, `PATCH /boletas/{id}/estado`, `DELETE /boletas/{id}` |
| ms-contacto | `POST /contactos`, `GET /contactos/mias`, `GET /contactos`, `GET /contactos/resumen`, `GET /contactos/{id}`, `PATCH /contactos/{id}`, `DELETE /contactos/{id}` |

Cada servicio documenta sus endpoints en **Swagger**: `http://HOST:PUERTO/swagger-ui.html`, con título, descripción y los códigos de respuesta de cada operación. Todos los errores tienen el mismo formato:

```json
{ "estado": 409, "mensaje": "El vehículo ya no está disponible.", "errores": {}, "fecha": "2026-11-16T19:42:00" }
```

## Configuración (`.env`)

Las claves **no están en el repositorio**. Se copia `.env.example` como `.env` y se completan todas las variables; `docker compose` se detiene con un aviso si falta alguna.

| Variable | Uso |
| --- | --- |
| `MYSQL_ROOT_CLAVE`, `DB_USUARIO`, `DB_CLAVE` | Acceso a MySQL |
| `JWT_SECRETO` | Secreto compartido para firmar y validar los JWT (`openssl rand -base64 48`) |
| `ADMIN_CORREO`, `ADMIN_CLAVE`, `VENDEDOR_CORREO`, `VENDEDOR_CLAVE` | Cuentas que ms-usuarios crea la primera vez que arranca |

## Ejecutar en local (Docker Desktop)

```powershell
Copy-Item .env.example .env
notepad .env          # completar las claves y el JWT_SECRETO
docker compose up --build -d
docker compose ps
```

La primera compilación tarda varios minutos. Luego Swagger queda en `http://localhost:8081/swagger-ui.html` (y 8082, 8083, 8084).

## Despliegue en AWS EC2

El backend corre en una instancia EC2 del AWS Academy Learner Lab con la **IP elástica `34.234.168.134`**, siguiendo la [guía del curso](https://github.com/JavPenaR/react-springboot4-docker-ec2). Swagger: `http://34.234.168.134:8081/swagger-ui.html` (y 8082, 8083, 8084).

1. **Instancia:** Amazon Linux 2023, x86_64, **t3.medium** (4 GiB de RAM) y 20 GiB de disco.
2. **IP elástica:** asociada a la instancia, así que la dirección no cambia al reiniciar el Learner Lab.
3. **Grupo de seguridad:** **TCP 8081-8084** abierto a internet, porque el frontend publicado en S3 llama a la API desde el navegador de cada visitante (las rutas privadas exigen JWT). SSH (22) solo desde las IP autorizadas. MySQL no se expone a internet.
4. **Docker:** instalar Docker, Docker Compose y Buildx (sección 3 de la guía).
5. **Código y configuración:**
   ```bash
   cd autofacil-backend
   cp .env.example .env
   sed -i "s|^JWT_SECRETO=.*|JWT_SECRETO=$(openssl rand -base64 48)|" .env
   nano .env                     # cambiar las claves de MySQL y de las cuentas iniciales
   docker compose up --build -d
   docker compose ps             # mysql y los 4 ms-* en estado Up
   ```
6. **Frontend:** se compila apuntando a `http://34.234.168.134:8081/api/v1` (y 8082, 8083, 8084) y se publica en S3: http://autofacil-frontend-bastian-espejo.s3-website-us-east-1.amazonaws.com

Los contenedores se reinician solos al encender la instancia. Para aplicar cambios de código: `docker compose up --build -d`. Para ver errores: `docker compose logs --tail=100 ms-ventas`.

## Pruebas

Cada microservicio tiene pruebas de integración que levantan la aplicación completa con una base H2 en memoria y recorren los flujos reales: roles, validaciones, reglas de negocio y errores. **JaCoCo** exige un mínimo de **80% de cobertura**: `mvn verify` falla si no se cumple.

```powershell
Set-Location .\ms-ventas
.\mvnw.cmd verify     # requiere Java 21; el reporte queda en target/site/jacoco/index.html
```

## Tecnologías

Spring Boot 4.0.9 · Java 21 · Spring Data JPA · Spring Security · JJWT · springdoc-openapi (Swagger) · MySQL 8.4 · H2 (pruebas) · JaCoCo · Lombok · Docker · Docker Compose · AWS EC2 (IP elástica) · AWS S3 (frontend)
