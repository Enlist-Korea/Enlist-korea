import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // 외부 접속 허용 (0.0.0.0)
    proxy: {
      // '/api'로 시작하는 요청이 오면 8080 포트로 보냄
      "/api": {
        target: "http://localhost:8080", // 백엔드 주소 (노트북 내부에서는 localhost로 통신)
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
