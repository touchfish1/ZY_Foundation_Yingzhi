<template>
  <section class="stats">
    <h2 v-if="props.props.title">{{ props.props.title }}</h2>
    <div class="grid">
      <div v-for="(item, i) in items" :key="i" class="stat">
        <span class="value">{{ item.value }}</span>
        <span class="label">{{ item.label }}</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

// 统计数据展示区块组件
const props = defineProps<{ props: Record<string, unknown> }>()

// 从 props 中提取统计项列表，保证返回数组
const items = computed(() => {
  const raw = props.props.items
  return Array.isArray(raw) ? raw : []
})

onMounted(() => {
  console.log('[Block] StatsBlock mounted')
})
</script>

<style scoped>
.stats {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto 96px;
  text-align: center;
}

.stats h2 {
  margin-bottom: 40px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.stat {
  padding: 32px 16px;
  background: #f8fafc;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.value {
  font-size: 36px;
  font-weight: 800;
  color: #0a0a0a;
}

.label {
  font-size: 14px;
  color: #64748b;
}
</style>
