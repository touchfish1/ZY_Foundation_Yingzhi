<template>
  <div class="doc-search" @click.self="$emit('close')">
    <div class="search-modal">
      <div class="search-input-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          ref="inputRef"
          v-model="query"
          type="text"
          class="search-input"
          placeholder="搜索文档..."
          @keydown.esc="$emit('close')"
        />
        <button class="search-close" @click="$emit('close')">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12" /></svg>
        </button>
      </div>
      <div class="search-results" v-if="query">
        <div v-if="filtered.length" class="results-list">
          <NuxtLink
            v-for="item in filtered"
            :key="item.to"
            :to="item.to"
            class="result-item"
            @click="$emit('close')"
          >
            <span class="result-title">{{ item.title }}</span>
            <span class="result-path">{{ item.to }}</span>
          </NuxtLink>
        </div>
        <div v-else class="search-empty">未找到相关文档</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

defineEmits<{ close: [] }>()

const query = ref('')
const inputRef = ref<HTMLInputElement>()

const allDocs = ref<{ title: string; to: string }[]>([])

const filtered = computed(() => {
  if (!query.value) return []
  const q = query.value.toLowerCase()
  return allDocs.value.filter(d => d.title.toLowerCase().includes(q))
})

onMounted(async () => {
  inputRef.value?.focus()
  try {
    const config = useRuntimeConfig()
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/pages/list?type=doc&page=1&pageSize=50`)
    if (res.code === 0) {
      allDocs.value = (res.data.items || []).map((d: any) => ({
        title: d.title,
        to: `/docs/${d.slug.replace(/^\/docs\//, '')}`
      }))
    }
  } catch (e) { /* silent */ }
})
</script>

<style scoped>
.doc-search {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: rgba(0,0,0,0.4);
  padding-top: 80px;
}
.search-modal {
  width: 560px;
  max-width: 90vw;
  background: var(--vp-c-bg-elavate);
  border: 1px solid var(--vp-c-border);
  border-radius: 12px;
  box-shadow: var(--vp-shadow-3);
  overflow: hidden;
}
.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--vp-c-divider);
}
.search-icon {
  flex-shrink: 0;
  color: var(--vp-c-text-3);
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  font-family: var(--vp-font-family-base);
  color: var(--vp-c-text);
  background: transparent;
}
.search-input::placeholder { color: var(--vp-c-text-3); }
.search-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: var(--vp-c-bg-mute);
  color: var(--vp-c-text-2);
  cursor: pointer;
}
.search-results { max-height: 320px; overflow-y: auto; }
.results-list { padding: 8px; }
.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  text-decoration: none;
  transition: background 0.15s;
}
.result-item:hover { background: var(--vp-c-brand-dimmer); }
.result-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--vp-c-text);
}
.result-path {
  font-size: 12px;
  color: var(--vp-c-text-3);
  font-family: var(--vp-font-family-mono);
}
.search-empty {
  padding: 32px;
  text-align: center;
  font-size: 14px;
  color: var(--vp-c-text-3);
}
</style>
