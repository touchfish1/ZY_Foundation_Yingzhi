<template>
  <main class="page-shell">
    <header class="nav">
      <div class="nav-inner section-paper">
        <NuxtLink to="/" class="brand">
          <span class="brand-icon">&#9670;</span>
          <span class="brand-text">ZHANGYUAN</span>
        </NuxtLink>
        <nav class="nav-links">
          <NuxtLink to="/" class="nav-link">首页</NuxtLink>
          <NuxtLink to="/plans" class="nav-link">定价</NuxtLink>
        </nav>
      </div>
    </header>

    <div class="page-content">
      <component
        :is="resolveBlock(block.type)"
        v-for="block in page.blocks"
        :key="block.id"
        :props="block.props"
      />
    </div>

    <footer class="site-footer" v-if="settings">
      <div class="footer-inner section-paper">
        <div class="footer-brand">
          <span class="footer-name">{{ settings.siteName || 'ZHANGYUAN' }}</span>
          <span class="footer-desc">{{ settings.siteDescription || 'API Platform' }}</span>
        </div>
        <div class="footer-meta">
          <span class="footer-text">{{ settings.footerText }}</span>
          <span class="footer-icp" v-if="settings.icpFiling">{{ settings.icpFiling }}</span>
        </div>
      </div>
    </footer>
  </main>
</template>

<script setup lang="ts">
import type { CmsBlock } from '~/types/cms'
import type { SiteSettings } from '~/composables/useSiteSettings'

// 动态 CMS 页面组件：根据 URL 路径渲染对应页面区块
const route = useRoute()
const slug = route.params.slug
// 解析路由参数：提取路径分段和语言环境
const segments = Array.isArray(slug) ? slug : [slug].filter(Boolean)
const locale = segments[0] === 'en' ? 'en-US' : 'zh-CN'
const pathSegments = locale === 'en-US' ? segments.slice(1) : segments
const path = `/${pathSegments.join('/')}` || '/'

console.log(`[Page] Rendering slug page: path=${path}, locale=${locale}`)

// 获取 CMS 页面数据（带缓存 key）
const { data: page } = await useAsyncData(
  `cms-${path}-${locale}`,
  () => useCmsPage(path, locale)
)
if (page.value) {
  console.log(`[Page] CMS data loaded for path=${path}`)
} else {
  console.warn(`[Page] CMS data not found for path=${path}`)
}

// 获取网站全局设置（站点名称、底部信息等）
const { data: settings } = await useAsyncData('settings', () => useSiteSettings())
if (settings.value) {
  console.log(`[Page] Site settings loaded`)
}

// 页面不存在时抛出 404 错误
const pageData = page.value
if (!pageData) {
  console.error(`[Page] 404: page not found for path=${path}`)
  throw createError({ statusCode: 404, statusMessage: 'Page not found' })
}

// 组件挂载后的生命周期回调
onMounted(() => {
  console.log(`[Page] ${path} mounted`)
})

// 设置页面 SEO 标题和 meta 信息
useHead({
  title: pageData.seo.title,
  meta: [
    { name: 'description', content: pageData.seo.description },
    { name: 'keywords', content: pageData.seo.keywords }
  ]
})

// 根据区块类型名映射到对应组件名，未知类型使用 UnknownBlock
function resolveBlock(type: CmsBlock['type']) {
  const map: Record<string, string> = {
    hero: 'HeroBlock',
    pricing: 'PricingBlock',
    faq: 'FaqBlock',
    cta: 'CtaBlock',
    'rich-text': 'RichTextBlock',
    'feature-grid': 'FeatureGridBlock',
    'image-banner': 'ImageBannerBlock',
    stats: 'StatsBlock'
  }
  return map[type] || 'UnknownBlock'
}
</script>

<style scoped>
.page-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.nav {
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 50;
}

.nav-inner {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 16px;
  color: #0a0a0a;
}

.brand-icon {
  color: #2563eb;
  font-size: 20px;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-link {
  color: #606060;
  font-size: 14px;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #0a0a0a;
}

.page-content {
  flex: 1;
}

.site-footer {
  background: #f8fafc;
  border-top: 1px solid #e8e8e8;
  color: #606060;
  font-size: 14px;
}

.footer-inner {
  padding: 32px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.footer-name {
  font-weight: 600;
  color: #0a0a0a;
}

.footer-desc {
  font-size: 13px;
}

.footer-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.footer-icp {
  color: #94a3b8;
}
</style>
