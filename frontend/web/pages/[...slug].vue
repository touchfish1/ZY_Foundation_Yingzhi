<template>
  <main class="page-shell">
    <div class="page-content">
      <component
        :is="resolveBlock(block.type)"
        v-for="block in pageData.blocks"
        :key="block.id"
        :props="block.props"
      />
    </div>
  </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })

import type { CmsBlock } from '~/types/cms'

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
  background: var(--vp-c-bg);
  font-family: var(--vp-font-family-base);
}

.page-content {
  flex: 1;
}


</style>
