import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    proxy: {
      // 代理所有以 /api 开头的请求
      "/api": {
        target: "http://your-backend-server.com:8080", // 你的后端地址
        changeOrigin: true,
        // 如果后端接口没有 /api 前缀，可以重写路径
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      "/captcha": {
        target: "http://your-captcha-server.com:8080",
        changeOrigin: true,
      },
    },
  },
});
