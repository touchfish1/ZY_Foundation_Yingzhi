<template>
  <div class="locale-switcher">
    <button
      v-for="t in translations"
      :key="t.locale"
      :class="['locale-btn', { active: t.locale === currentLocale }]"
      @click="$emit('select', t.locale)"
    >
      <span class="locale-code">{{ localeLabel(t.locale) }}</span>
      <span v-if="t.publishedVersionId" class="status-dot published" title="已发布">●</span>
      <span v-else-if="t.draftVersionId" class="status-dot draft" title="草稿">○</span>
      <span v-else class="status-dot new" title="未编辑">+</span>
    </button>
    <n-dropdown placement="bottom-start" trigger="click" :options="addLocaleOptions" @select="onAddLocale">
      <button class="locale-btn add-locale-btn" title="添加语言">
        <span style="font-weight:700;font-size:16px;line-height:1">+</span>
      </button>
    </n-dropdown>
  </div>
</template>

<script setup lang="ts">
// 语言切换器组件：显示所有翻译语言按钮，标记已发布/草稿/未编辑状态
import { computed } from 'vue'
import { NDropdown } from 'naive-ui'

const props = defineProps<{
  translations: Array<{ locale: string; draftVersionId?: number; publishedVersionId?: number; status?: string }>
  currentLocale: string
  availableLocales?: string[]
}>()

const emit = defineEmits<{
  select: [locale: string]
  addLocale: [locale: string]
}>()

const localeLabels: Record<string, string> = {
  'zh-CN': '中文',
  'en-US': 'English',
  'ja-JP': '日本語'
}

function localeLabel(code: string): string {
  return localeLabels[code] || code
}

const existingLocales = computed(() => props.translations.map(t => t.locale))
const defaultAvailable = ['zh-CN', 'en-US', 'ja-JP']

const addLocaleOptions = computed(() => {
  const available = (props.availableLocales || defaultAvailable)
    .filter(l => !existingLocales.value.includes(l))
  return available.map(l => ({
    label: `${localeLabel(l)} (${l})`,
    key: l
  }))
})

function onAddLocale(locale: string) {
  emit('addLocale', locale)
}
</script>

<style scoped>
.locale-switcher {
  display: flex;
  gap: 4px;
}
.locale-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;
}
.locale-btn:hover {
  border-color: #2080f0;
  color: #2080f0;
}
.locale-btn.active {
  background: #2080f0;
  color: #fff;
  border-color: #2080f0;
}
.locale-code {
  font-weight: 600;
}
.status-dot {
  font-size: 10px;
}
.status-dot.published {
  color: #22c55e;
}
.locale-btn.active .status-dot {
  color: #fff;
}
.status-dot.new {
  font-weight: 700;
}
.add-locale-btn {
  min-width: 32px;
  justify-content: center;
}
</style>
