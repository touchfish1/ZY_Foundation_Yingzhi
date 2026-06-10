export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  css: ['~/assets/css/main.css'],
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080',
      aiBase: process.env.NUXT_PUBLIC_AI_BASE || 'http://localhost:8086'
    }
  },
  nitro: {
    routeRules: {
      '/api/**': { proxy: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080' },
      '/v1/**': { proxy: process.env.NUXT_PUBLIC_AI_BASE || 'http://localhost:8086' }
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
      ],
      link: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: 'anonymous' }
      ],
      script: [
        {
          innerHTML: `(function(){var e=localStorage.getItem("vitepress-theme-appearance")||"auto",a=window.matchMedia("(prefers-color-scheme: dark)").matches;(e==="auto"?a:e==="dark")&&document.documentElement.classList.add("dark","vitepress-theme-appearance")})();`,
          tagPosition: 'head'
        }
      ]
    }
  }
})
