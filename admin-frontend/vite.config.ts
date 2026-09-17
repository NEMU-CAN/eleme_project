import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 1895,
    strictPort: true,
    proxy: {
      // 后端 context-path 为 /api，保留前缀直接透传即可。
      '/api': {
        target: process.env.VITE_BACKEND_TARGET ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
