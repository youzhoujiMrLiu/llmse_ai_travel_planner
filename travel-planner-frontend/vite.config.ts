import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      vueDevTools(),
      // 自定义插件：在 HTML 中替换环境变量
      {
        name: 'html-transform',
        transformIndexHtml(html) {
          // 替换高德地图 API Key
          return html.replace(/%VITE_AMAP_WEB_KEY%/g, env.VITE_AMAP_WEB_KEY || '')
        },
      },
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8080', // Spring Boot 地址
          changeOrigin: true,
          secure: false,
        }
      }
    }
  }
})
