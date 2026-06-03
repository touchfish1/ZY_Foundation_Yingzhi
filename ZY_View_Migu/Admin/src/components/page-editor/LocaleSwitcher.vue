<template>
  <div class="locale-switcher">
    <button
      v-for="t in translations"
      :key="t.locale"
      :class="['locale-btn', { active: t.locale === currentLocale }]"
      @click="$emit('select', t.locale)"
    >
      <span class="locale-code">{{ t.locale }}</span>
      <span v-if="!t.draftVersionId && t.locale === currentLocale" class="new-badge">可新建</span>
    </button>
  </div>
</template>

<script setup lang="ts">
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

.new-badge {
  font-size: 11px;
  background: rgba(255, 255, 255, 0.25);
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 400;
}
</style>
