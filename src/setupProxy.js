const { createProxyMiddleware } = require('http-proxy-middleware');

/**
 * Proxy de desarrollo: redirige /api/* → http://localhost:8080/api/*
 * El navegador solo habla con localhost:3000 — elimina CORS por completo.
 */
module.exports = function (app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      logLevel: 'warn',
    })
  );
};
