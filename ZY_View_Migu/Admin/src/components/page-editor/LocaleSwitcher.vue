<template>
  <div class="locale-switcher">
    <button
      v-for="t in translations"
      :key="t.locale"
      :class="['locale-btn', { active: t.locale === currentLocale }]"
      @click="$emit('select', t.locale)"
    >
      <span class="locale-code">{{ t.locale }}</span>
      <span v-if="t.publishedVersionId" class="status-dot published" title="已发布">●</span>
      <span v-else-if="t.draftVersionId" class="status-dot draft" title="草稿">○</span>
      <span v-else class="status-dot new" title="未编辑">+</span>
    </button>
  </div>
</template>

<script setup lang="ts">
// 语言切换器组件：显示所有翻译语言按钮，标记已发布/草稿/未编辑状态
defineProps<{
  translations: Array<{ locale: string; draftVersionId?: number; publishedVersionId?: number; status?: string }>
  currentLocale: string
}>()

defineEmits<{
  select: [locale: string]
}>()
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
</style>
