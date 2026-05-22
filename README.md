# Educational Control — Sistema de Planillas

> Sistema de gestión de planillas para instituciones educativas en El Salvador.
> Proyecto Final · Desarrollo de Aplicaciones con Web Frameworks · UDB Ciclo 3-2026

---

## Tabla de contenidos

1. [Descripción](#descripción)
2. [Equipo](#equipo)
3. [Tecnologías](#tecnologías)
4. [Arquitectura](#arquitectura)
5. [Requisitos previos](#requisitos-previos)
6. [Configuración y ejecución](#configuración-y-ejecución)
7. [Usuarios demo](#usuarios-demo)
8. [Endpoints principales](#endpoints-principales)
9. [Estructura del proyecto](#estructura-del-proyecto)
10. [Seguridad](#seguridad)

---

## Descripción

**Educational Control** es una aplicación web full-stack que permite a instituciones educativas administrar su planilla de empleados de forma segura y eficiente. Incluye:

- Gestión de empleados (CRUD completo con validación de DUI salvadoreño)
- Generación y consulta de planillas mensuales
- Administración de descuentos (ISSS, AFP, renta y otros)
- Control de acceso basado en roles (Administrador, RRHH, Empleado)
- Autenticación mediante JWT (stateless, sin sesiones en servidor)
- Documentación interactiva de la API con Swagger UI

---

## Equipo

| Nombre                  | Rol              |
|-------------------------|------------------|
| Gaby Figueroa           | Desarrolladora   |
| Daniela Nicole Garcia   | Desarrolladora   |
| Josue Mena              | Desarrollador    |

**Docente:** Lic. Ricardo Gomez
**Asignatura:** Desarrollo de Aplicaciones con Web Frameworks
**Institución:** Universidad Don Bosco (UDB) · Ciclo 3-2026

---

## Tecnologías

### Backend

| Tecnología              | Versión   | Propósito                                    |
|-------------------------|-----------|----------------------------------------------|
| Java                    | 17        | Lenguaje principal                           |
| Spring Boot             | 3.2.0     | Framework de aplicación                      |
| Spring Security         | 6.x       | Autenticación y autorización (JWT + RBAC)    |
| Spring Data JPA         | 3.2.0     | Acceso a datos / ORM                         |
| Hibernate               | 6.x       | Implementación JPA                           |
| JJWT (io.jsonwebtoken)  | 0.12.x    | Generación y validación de tokens JWT        |
| MySQL Connector/J       | 8.x       | Driver de base de datos                      |
| Maven Wrapper           | 3.9       | Gestor de dependencias y build               |
| Springdoc OpenAPI       | 2.x       | Documentación automática (Swagger UI)        |
| Lombok                  | 1.18.x    | Reducción de código boilerplate              |
| Jakarta Validation      | 3.x       | Validaciones de entrada (Bean Validation)    |

### Frontend

| Tecnología              | Versión   | Propósito                                    |
|-------------------------|-----------|----------------------------------------------|
| React                   | 19.x      | Librería de interfaz de usuario              |
| Create React App        | 5.0.1     | Toolchain de desarrollo                      |
| CSS personalizado        | —         | Sistema de diseño sin frameworks externos    |
| Barlow (Google Fonts)   | —         | Tipografía del sistema                       |
| http-proxy-middleware   | 2.0.9     | Proxy de desarrollo (elimina CORS)           |

### Base de datos

| Tecnología | Versión | Propósito                |
|------------|---------|--------------------------|
| MySQL      | 8.x     | Base de datos relacional |

---

## Arquitectura

```
┌─────────────────────────────────────────────────────┐
│                    Navegador                        │
│              http://localhost:3000                  │
│                                                     │
│  ┌────────────────────────────────────────────┐    │
│  │          React SPA (Create React App)       │    │
│  │   Componentes · Estado · Llamadas /api/*    │    │
│  └──────────────────┬─────────────────────────┘    │
└─────────────────────┼───────────────────────────────┘
                      │  Proxy transparente
                      │  (setupProxy.js — sin CORS)
                      ▼
┌─────────────────────────────────────────────────────┐
│           Spring Boot API — :8080/api               │
│                                                     │
│  JwtAuthenticationFilter → Controllers → Services   │
│  AdviceController (manejo centralizado de errores)  │
└──────────────────────┬──────────────────────────────┘
                       │  Spring Data JPA / Hibernate
                       ▼
┌─────────────────────────────────────────────────────┐
│          MySQL 8 — sistema_planilla                 │
└─────────────────────────────────────────────────────┘
```

---

## Requisitos previos

- **Java 17** (JDK) — se recomienda Microsoft Build of OpenJDK 17
- **MySQL 8** con la base de datos `sistema_planilla` creada
- **Node.js 18+** y **npm 9+**
- Maven no es necesario instalarlo globalmente (el proyecto incluye `mvnw`)

---

## Configuración y ejecución

### 1. Base de datos

Crear la base de datos en MySQL Workbench (o cliente MySQL):

```sql
CREATE DATABASE sistema_planilla CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Las tablas se crean automáticamente al iniciar el backend (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend

> **Nota:** Si `JAVA_HOME` no está configurado en las variables de entorno del sistema,
> se debe pasar explícitamente al proceso usando el bloque PowerShell siguiente.

```powershell
$javaHome    = "C:\Users\joshm\.jdks\ms-17.0.19"
$backendPath = "<ruta_del_proyecto>\backend"

Start-Process -FilePath "$backendPath\mvnw.cmd" `
              -ArgumentList "spring-boot:run" `
              -WorkingDirectory $backendPath `
              -Environment @{ JAVA_HOME = $javaHome; PATH = "$javaHome\bin;$env:PATH" }
```

El backend estará disponible en:

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

### 3. Frontend

```powershell
cd "<ruta_del_proyecto>\frontend"
npm install       # solo la primera vez
npm start
```

La aplicación estará disponible en: `http://localhost:3000`

> El proxy de desarrollo (`setupProxy.js`) redirige automáticamente
> `/api/*` → `http://localhost:8080/api/*`, eliminando por completo los problemas de CORS.

---

## Usuarios demo

Al iniciar el backend se crean automáticamente tres cuentas de prueba:

| Usuario    | Rol           | Contraseña     |
|------------|---------------|----------------|
| `admin`    | ROLE_ADMIN    | `Admin123!`    |
| `rrhh`     | ROLE_RRHH     | `Rrhh123!`     |
| `empleado` | ROLE_EMPLEADO | `Empleado123!` |

---

## Endpoints principales

| Método | Ruta                    | Descripción                         | Roles requeridos        |
|--------|-------------------------|-------------------------------------|-------------------------|
| POST   | `/auth/login`           | Autenticación, devuelve JWT         | Público                 |
| GET    | `/empleados`            | Listar empleados activos            | ADMIN, RRHH             |
| POST   | `/empleados`            | Crear empleado                      | ADMIN, RRHH             |
| PUT    | `/empleados/{id}`       | Actualizar empleado                 | ADMIN, RRHH             |
| DELETE | `/empleados/{id}`       | Dar de baja empleado                | ADMIN                   |
| GET    | `/planillas`            | Listar planillas                    | ADMIN, RRHH             |
| POST   | `/planillas`            | Generar planilla mensual            | ADMIN, RRHH             |
| GET    | `/planillas/{id}`       | Detalle de planilla                 | ADMIN, RRHH, EMPLEADO   |
| GET    | `/descuentos`           | Listar tipos de descuento           | ADMIN, RRHH             |
| POST   | `/descuentos`           | Crear tipo de descuento             | ADMIN                   |

> Todos los endpoints (excepto `/auth/login`) requieren el header:
> `Authorization: Bearer <token>`
>
> La documentación completa está disponible en Swagger UI una vez levantado el backend.

---

## Estructura del proyecto

```
SistemaPlanilla/
├── backend/
│   ├── src/main/java/sv/edu/udb/
│   │   ├── config/          # Configuración de seguridad y JWT
│   │   ├── controller/      # Controladores REST + AdviceController
│   │   ├── dto/
│   │   │   ├── request/     # EmpleadoRequest, PlanillaRequest, DescuentoRequest, etc.
│   │   │   └── response/    # ApiErrorResponse y demás respuestas
│   │   ├── model/           # Entidades JPA (Empleado, Planilla, Descuento, Usuario…)
│   │   ├── repository/      # Repositorios Spring Data JPA
│   │   ├── security/        # JwtTokenProvider, JwtAuthenticationFilter
│   │   ├── service/         # Lógica de negocio
│   │   └── validation/      # @ValidDui y validadores personalizados
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   ├── public/
│   └── src/
│       ├── App.js           # Componente raíz · lógica y vistas de la SPA
│       ├── index.css        # Sistema de diseño (tokens CSS, estilo Stripe)
│       └── setupProxy.js    # Proxy de desarrollo (elimina CORS)
└── README.md
```

---

## Seguridad

- Contraseñas cifradas con **BCrypt**.
- Tokens **JWT** firmados con HMAC-SHA256 y validados en cada petición protegida.
- Separación de acceso por roles: `ADMIN`, `RRHH` y `EMPLEADO`.
- Validaciones en DTOs con mensajes de error específicos por campo.
- Manejo centralizado de excepciones que nunca expone detalles internos al cliente.
- Secreto JWT configurable mediante variable de entorno `JWT_SECRET`.

---

*Educational Control — Universidad Don Bosco · UDB 2026*
