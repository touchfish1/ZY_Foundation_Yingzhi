<template>
  <main class="page-shell">
    <header class="nav">
      <strong>ZHANGYUAN</strong>
      <span>API Platform</span>
    </header>

    <component
      :is="resolveBlock(block.type)"
      v-for="block in page.blocks"
      :key="block.id"
      :props="block.props"
    />
  </main>
</template>

<script setup lang="ts">
import type { CmsBlock } from '~/types/cms'

const route = useRoute()
const slug = route.params.slug
const segments = Array.isArray(slug) ? slug : [slug].filter(Boolean)
const locale = segments[0] === 'en' ? 'en-US' : 'zh-CN'
const pathSegments = locale === 'en-US' ? segments.slice(1) : segments
const path = `/${pathSegments.join('/') || 'home'}`

const page = await useCmsPage(path, locale)

useHead({
  title: page.seo.title,
  meta: [
    { name: 'description', content: page.seo.description },
    { name: 'keywords', content: page.seo.keywords }
  ]
})

function resolveBlock(type: CmsBlock['type']) {
  const map: Record<string, string> = {
    hero: 'HeroBlock',
    pricing: 'PricingBlock',
    faq: 'FaqBlock',
    cta: 'CtaBlock',
    'rich-text': 'RichTextBlock',
    'feature-grid': 'FeatureGridBlock'
  }
  return map[type] || 'UnknownBlock'
}
</script>

<style scoped>
.page-shell {
  min-height: 100vh;
  overflow: hidden;
  background: #fff;
}

.nav {
  width: min(1120px, calc(100% - 40px));
  height: 72px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606060;
  border-bottom: 1px solid #e8e8e8;
}

.nav strong {
  color: #0a0a0a;
  letter-spacing: 0.08em;
}
</style>
