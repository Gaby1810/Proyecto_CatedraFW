# Educational Control — Frontend

Interfaz web del Sistema de Planillas Educational Control, desarrollada con **React 19** y **Create React App**.

## Tecnologías

| Tecnología             | Versión  | Propósito                                  |
|------------------------|----------|--------------------------------------------|
| React                  | 19.x     | Librería de interfaz de usuario            |
| Create React App       | 5.0.1    | Toolchain de desarrollo                    |
| CSS personalizado       | —        | Sistema de diseño sin frameworks externos  |
| Barlow (Google Fonts)  | —        | Tipografía del sistema                     |
| http-proxy-middleware  | 2.0.9    | Proxy de desarrollo (elimina CORS)         |

## Estructura

```
src/
├── App.js          # Componente raíz · todas las vistas y lógica de la SPA
├── index.css       # Sistema de diseño (tokens CSS, estilo Stripe)
├── index.js        # Punto de entrada React
└── setupProxy.js   # Proxy /api/* → http://localhost:8080 (solo desarrollo)
```

## Comandos disponibles

### `npm start`

Levanta el servidor de desarrollo en [http://localhost:3000](http://localhost:3000).

Las peticiones a `/api/*` se redirigen automáticamente al backend en `http://localhost:8080` a través del proxy configurado en `setupProxy.js`.

### `npm run build`

Genera la build optimizada para producción en la carpeta `build/`.

### `npm test`

Ejecuta las pruebas en modo interactivo.

## Proxy de desarrollo

El archivo `src/setupProxy.js` configura un proxy transparente que redirige
todas las peticiones `/api/*` al backend Spring Boot en `http://localhost:8080`.
Esto elimina los problemas de CORS durante el desarrollo sin necesidad de modificar
la configuración del servidor.

```
Navegador (localhost:3000)
        │
        │  fetch('/api/...')
        ▼
React Dev Server (proxy)
        │
        │  → http://localhost:8080/api/...
        ▼
Spring Boot API
```

## Variables de entorno

No se requieren variables de entorno para desarrollo local. El proxy maneja
la redirección automáticamente.

---

Ver el [README principal](../README.md) para instrucciones completas de instalación
y ejecución del proyecto completo.
