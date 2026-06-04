import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      '/admin': 'http://localhost:8088',
      '/api': 'http://localhost:8088',
      '/actuator': 'http://localhost:8088'
    }
  }
})
