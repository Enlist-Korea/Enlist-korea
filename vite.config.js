// vite.config.js
// [역할] 개발 중 프론트의 '/api' 호출을 백엔드로 프록시(CORS 회피)
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080", // ← 실제 백엔드 주소로 교체
        changeOrigin: true,
        // 필요 시 secure:false, rewrite 등 추가
      },
    },
  },
});
