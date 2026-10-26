import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  return {
    plugins: [react()],
    server: {
      port: 5173,
      strictPort: true,
      proxy: Object.fromEntries(
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
  };
});
