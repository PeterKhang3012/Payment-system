import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from 'node:path';
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  return {
    plugins: [react(), {
      name: 'kdn-docs-route',
      configureServer(server) {
        server.middlewares.use((req, _res, next) => {
          if (req.url && /^\/docs\/?(?:\?|$)/.test(req.url)) {
            req.url = req.url.replace(/^\/docs\/?/, '/docs.html');
          }
          next();
        });
      },
    }],
    build: {
      rollupOptions: { input: { app: resolve(__dirname, 'index.html'), docs: resolve(__dirname, 'docs.html') } },
    },
    server: {
      port: 5173,
      strictPort: true,
      proxy: {
        ...Object.fromEntries(Object.entries({
          auth: env.AUTH_TARGET || 'http://localhost:3000',
          user: env.USER_TARGET || 'http://localhost:3006',
          balance: env.BALANCE_TARGET || 'http://localhost:3001',
          tuition: env.TUITION_TARGET || 'http://localhost:3002',
          payment: env.PAYMENT_TARGET || 'http://localhost:8080',
          otp: env.OTP_TARGET || 'http://localhost:8083',
          email: env.EMAIL_TARGET || 'http://localhost:3007',
        }).map(([key, target]) => [`/docs-api/${key}`, {
          target, changeOrigin: true,
          rewrite: (path: string) => path.replace(new RegExp(`^/docs-api/${key}(?=/|$)`), ''),
        }])),
        ...Object.fromEntries(
        Object.entries({
          auth: env.AUTH_TARGET || "http://auth-service:3000",
          user: env.USER_TARGET || "http://user-service:3006",
          balance: env.BALANCE_TARGET || "http://balance-management:3001",
          tuition: env.TUITION_TARGET || "http://tuition:3002",
          payment: env.PAYMENT_TARGET || "http://payment:8080",
        }).map(([key, target]) => [
          `/gateway/${key}`,
          {
            target,
            changeOrigin: true,
            rewrite: (path: string) => path.replace(`/gateway/${key}`, ""),
          },
        ]),
        ),
      },
    },
  };
});
