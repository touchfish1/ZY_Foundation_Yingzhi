<template>
  <nav class="doc-sidebar">
    <div class="sidebar-group" v-for="group in docGroups" :key="group.title">
      <h3 class="sidebar-group-title">{{ group.title }}</h3>
      <NuxtLink
        v-for="item in group.items"
        :key="item.slug"
        :to="item.to"
        class="sidebar-link"
        :class="{ active: isActive(item.to) }"
        @click="$emit('navigate')"
      >
        {{ item.title }}
      </NuxtLink>
    </div>

    <div v-if="loading" class="sidebar-loading">加载中...</div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

defineEmits<{ navigate: [] }>()

const route = useRoute()

interface DocItem {
  slug: string
  title: string
  to: string
}

const loading = ref(true)
const docs = ref<DocItem[]>([])

const docGroups = computed(() => {
  const groups: { title: string; items: DocItem[] }[] = []

  // Quick start group
  const quickStart = docs.value.filter(d => d.slug.startsWith('/docs/quick-start'))
  if (quickStart.length) {
    groups.push({ title: '快速开始', items: quickStart })
  }

  // API Reference group
  const apiRef = docs.value.filter(d => d.slug.startsWith('/docs/api-reference'))
  if (apiRef.length) {
    groups.push({ title: 'API 参考', items: apiRef })
  }

  // Others
  const others = docs.value.filter(d =>
    !d.slug.startsWith('/docs/quick-start') && !d.slug.startsWith('/docs/api-reference')
  )
  if (others.length) {
    groups.push({ title: '其他', items: others })
  }

  // Always show default groups if no CMS data
  if (!docs.value.length) {
    return [
      {
        title: '入门',
        items: [
          { slug: '/docs/quick-start', title: '快速开始', to: '/docs' },
          { slug: '/docs/api-reference', title: 'API 参考', to: '/docs' }
        ]
      }
    ]
  }

  return groups
})

function isActive(to: string): boolean {
  return route.path === to
}

onMounted(async () => {
  try {
    const config = useRuntimeConfig()
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/pages/list?type=doc&page=1&pageSize=50`)
    if (res.code === 0) {
      docs.value = (res.data.items || []).map((d: any) => ({
        slug: d.slug,
        title: d.title,
        to: `/docs/${d.slug.replace(/^\/docs\//, '')}`
      }))
    }
  } catch (e) {
    // Silently fall back to defaults
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.doc-sidebar {
  padding: 0 8px;
}
.sidebar-group {
  margin-bottom: 24px;
}
.sidebar-group-title {
  padding: 0 12px;
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 700;
  color: var(--vp-c-text-3);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.sidebar-link {
  display: block;
  padding: 7px 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--vp-c-text-2);
  text-decoration: none;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
  line-height: 1.5;
}
.sidebar-link:hover {
  color: var(--vp-c-brand);
  background: var(--vp-c-brand-dimmer);
}
.sidebar-link.active {
  color: var(--vp-c-brand);
  font-weight: 600;
  background: var(--vp-c-brand-dimmer);
}
.sidebar-loading {
  padding: 12px;
  font-size: 13px;
  color: var(--vp-c-text-3);
}
</style>
