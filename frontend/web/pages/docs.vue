<template>
  <div class="doc-page">
    <!-- Doc listing -->
    <div v-if="!selectedDoc" class="doc-index">
      <div class="index-header">
        <h1 class="index-title">开发者文档</h1>
        <p class="index-desc">快速集成，轻松上手。从入门到高级，尽在文档中心。</p>
        <div class="index-search">
          <DocSearch @close="searchOpen = false" v-if="searchOpen" />
          <button class="search-trigger" @click="searchOpen = true">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
            </svg>
            搜索文档...
            <span class="search-hint">⌘K</span>
          </button>
        </div>
      </div>

      <div class="index-grid">
        <NuxtLink v-for="doc in allDocs" :key="doc.to" :to="doc.to" class="doc-card">
          <h3 class="doc-card-title">{{ doc.title }}</h3>
          <p class="doc-card-desc">{{ doc.description || '查看文档' }}</p>
          <span class="doc-card-arrow">→</span>
        </NuxtLink>
      </div>
    </div>

    <!-- Single doc view -->
    <div v-else class="doc-view">
      <article class="doc-article">
        <h1>{{ selectedDoc.title }}</h1>
        <div class="doc-meta">
          <time v-if="selectedDoc.updatedAt">更新于 {{ formatDate(selectedDoc.updatedAt) }}</time>
        </div>
        <div class="doc-body" v-html="selectedDoc.content || '<p>文档内容加载中...</p>'" />
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'docs' })
usePageMeta('开发者文档', '快速集成，轻松上手。从入门到高级，尽在文档中心。')

import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const searchOpen = ref(false)

interface DocPage {
  title: string
  slug: string
  description?: string
  content?: string
  updatedAt?: string
  to: string
}

const allDocs = ref<DocPage[]>([])
const docContent = ref<Record<string, DocPage>>({})

const selectedDoc = computed(() => {
  // Check if we're on a specific doc path
  const slug = route.path.replace('/docs/', '')
  if (!slug || slug === 'docs') return null
  return docContent.value[slug] || null
})

function formatDate(dateStr: string): string {
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch { return dateStr }
}

async function loadDoc(slug: string) {
  if (docContent.value[slug]) return
  try {
    const config = useRuntimeConfig()
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/page?slug=/docs/${slug}`)
    if (res.code === 0 && res.data) {
      docContent.value[slug] = {
        title: res.data.title,
        slug: res.data.slug,
        content: res.data.content,
        updatedAt: res.data.updatedAt,
        to: `/docs/${slug}`
      }
    }
  } catch (e) {
    console.error('Failed to load doc', e)
  }
}

onMounted(async () => {
  // Load doc list
  try {
    const config = useRuntimeConfig()
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/pages/list?type=doc&page=1&pageSize=50`)
    if (res.code === 0) {
      allDocs.value = (res.data.items || []).map((d: any) => ({
        title: d.title || d.slug?.split('/').pop() || '文档',
        slug: d.slug?.replace(/^\/docs\//, '') || '',
        description: d.description || '',
        updatedAt: d.updatedAt,
        to: `/docs/${d.slug?.replace(/^\/docs\//, '') || ''}`
      }))
    }
  } catch (e) { /* silent */ }

  // Check if current route is a doc detail
  const slug = route.path.replace('/docs/', '')
  if (slug && slug !== 'docs') {
    await loadDoc(slug)
  }
})

// Watch for route changes
watch(() => route.path, async (path) => {
  const slug = path.replace('/docs/', '')
  if (slug && slug !== 'docs') {
    await loadDoc(slug)
  }
})
</script>

<style scoped>
.doc-page {
  min-height: calc(100vh - var(--vp-nav-height));
}

/* ===== Index / Listing ===== */
.doc-index {
  padding: 48px 0;
}
.index-header {
  margin-bottom: 48px;
}
.index-title {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-text);
  margin: 0 0 12px;
  line-height: 1.2;
  font-family: var(--vp-font-family-display);
}
.index-desc {
  font-size: 16px;
  color: var(--vp-c-text-2);
  margin: 0 0 24px;
  line-height: 1.6;
}
.search-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid var(--vp-c-border);
  border-radius: 8px;
  background: var(--vp-c-bg);
  color: var(--vp-c-text-3);
  font-size: 14px;
  font-family: var(--vp-font-family-base);
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
  min-width: 260px;
}
.search-trigger:hover {
  border-color: var(--vp-c-brand);
  color: var(--vp-c-text-2);
}
.search-hint {
  margin-left: auto;
  font-size: 11px;
  font-family: var(--vp-font-family-mono);
  color: var(--vp-c-text-3);
  padding: 1px 6px;
  border: 1px solid var(--vp-c-border);
  border-radius: 4px;
}

.index-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.doc-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1px solid var(--vp-c-border);
  border-radius: 12px;
  text-decoration: none;
  background: var(--vp-c-bg);
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}
.doc-card:hover {
  border-color: var(--vp-c-brand);
  box-shadow: var(--vp-shadow-2);
  transform: translateY(-2px);
}
.doc-card-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--vp-c-text);
}
.doc-card-desc {
  margin: 0;
  font-size: 14px;
  color: var(--vp-c-text-3);
  line-height: 1.5;
  flex: 1;
}
.doc-card-arrow {
  margin-top: 12px;
  font-size: 14px;
  color: var(--vp-c-brand);
  font-weight: 600;
}

/* ===== Single Doc View ===== */
.doc-article {
  max-width: 720px;
}
.doc-article h1 {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-text);
  margin: 0 0 8px;
  line-height: 1.2;
}
.doc-meta {
  margin-bottom: 32px;
  font-size: 13px;
  color: var(--vp-c-text-3);
}

/* ===== Doc body content styling ===== */
.doc-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--vp-c-text);
}
.doc-body :deep(h2) {
  font-size: 24px;
  font-weight: 700;
  margin: 40px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--vp-c-divider);
  color: var(--vp-c-text);
}
.doc-body :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  margin: 32px 0 12px;
  color: var(--vp-c-text);
}
.doc-body :deep(p) {
  margin: 0 0 16px;
  color: var(--vp-c-text);
}
.doc-body :deep(ul), .doc-body :deep(ol) {
  margin: 0 0 16px;
  padding-left: 24px;
}
.doc-body :deep(li) {
  margin-bottom: 6px;
}
.doc-body :deep(code) {
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--vp-code-bg);
  color: var(--vp-code-color);
}
.doc-body :deep(pre) {
  margin: 16px 0;
  padding: 16px 20px;
  border-radius: 10px;
  background: var(--vp-code-block-bg);
  border: 1px solid var(--vp-c-border);
  overflow-x: auto;
}
.doc-body :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--vp-c-text);
}
.doc-body :deep(a) {
  color: var(--vp-c-brand);
  text-decoration: none;
}
.doc-body :deep(a:hover) {
  text-decoration: underline;
}
.doc-body :deep(blockquote) {
  margin: 16px 0;
  padding: 8px 16px;
  border-left: 3px solid var(--vp-c-brand);
  color: var(--vp-c-text-2);
  background: var(--vp-c-bg-soft);
  border-radius: 0 8px 8px 0;
}
.doc-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}
.doc-body :deep(th) {
  background: var(--vp-c-bg-soft);
  font-weight: 600;
  text-align: left;
  padding: 10px 16px;
  border: 1px solid var(--vp-c-divider);
  font-size: 13px;
  color: var(--vp-c-text);
}
.doc-body :deep(td) {
  padding: 10px 16px;
  border: 1px solid var(--vp-c-divider);
  font-size: 14px;
}
.doc-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--vp-c-divider);
  margin: 32px 0;
}
.doc-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .index-grid { grid-template-columns: 1fr; }
  .index-title { font-size: 28px; }
  .search-trigger { min-width: auto; width: 100%; }
}
</style>
