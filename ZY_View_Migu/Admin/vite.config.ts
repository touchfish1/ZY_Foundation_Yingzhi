import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/admin': 'http://localhost:8080',
      '/api': 'http://localhost:8080',
      '/actuator': 'http://localhost:8080'
    }
  }
})
