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
  background:
    radial-gradient(circle at 15% 10%, rgba(56, 189, 248, 0.24), transparent 28%),
    radial-gradient(circle at 80% 20%, rgba(99, 102, 241, 0.26), transparent 30%),
    linear-gradient(135deg, #07111f, #101827 50%, #06151a);
}

.nav {
  width: min(1120px, calc(100% - 40px));
  height: 72px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: rgba(248, 250, 252, 0.78);
}

.nav strong {
  color: #fff;
  letter-spacing: 0.08em;
}
</style>
