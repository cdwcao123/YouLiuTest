import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

/**
 * Vite 配置：
 * - @ 别名指向 src 目录，方便 import '@/xxx'
 * - 开发服务器端口 5173，/api 请求代理到 Java 后端 http://localhost:8080
 */
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Java / Spring Boot 默认端口
        changeOrigin: true,
      },
    },
  },
})
