export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  css: ['~/assets/css/main.css'],
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080'
    }
  },
  app: {
    pageTransition: { name: 'page', mode: 'out-in' },
    head: {
      title: 'ZHANGYUAN API Platform',
      meta: [
        { name: 'description', content: '稳定、高性能的 API 服务平台' },
        { name: 'keywords', content: 'API, 云服务, 开发者工具' },
        { name: 'robots', content: 'index, follow' }
      ]
    }
  }
})
