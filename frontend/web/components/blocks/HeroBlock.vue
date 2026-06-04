<template>
  <section class="hero">
    <p class="eyebrow">Modern API Infrastructure</p>
    <h1>{{ props.title || '稳定、高性能的 API 服务' }}</h1>
    <p class="subtitle">{{ props.subtitle || '为开发者和团队提供可扩展的 API 能力。' }}</p>
    <div class="actions">
      <a class="primary" :href="String(props.primaryButtonUrl || '#plans')">{{ props.primaryButtonText || '查看套餐' }}</a>
      <a class="secondary" href="#faq">了解更多</a>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

// 首页英雄横幅区块组件，包含大标题、副标题和操作按钮
defineProps<{ props: Record<string, unknown> }>()

onMounted(() => {
  console.log('[Block] HeroBlock mounted')
})
</script>

<style scoped>
.hero {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto;
  padding: 96px 0 80px;
  text-align: center;
  background: linear-gradient(135deg, #e0f2fe 0%, #e0e7ff 50%, #ede9fe 100%);
  border-radius: 0.75rem;
  margin-top: 40px;
}

.eyebrow {
  color: #606060;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 14px;
  margin-bottom: 16px;
}

h1 {
  max-width: 800px;
  margin: 0 auto 20px;
  font-size: clamp(36px, 6vw, 60px);
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: #0a0a0a;
}

.subtitle {
  max-width: 600px;
  margin: 0 auto;
  color: #606060;
  font-size: 18px;
  line-height: 1.6;
}

.actions {
  margin-top: 36px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

a {
  padding: 12px 24px;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.2s;
}

.primary {
  color: #fafafa;
  background: #070707;
}

.primary:hover {
  opacity: 0.85;
}

.secondary {
  border: 1px solid #070707;
  color: #0a0a0a;
}

.secondary:hover {
  background: #f5f5f5;
}

@media (max-width: 768px) {
  .hero {
    padding: 48px 20px 40px;
    margin-top: 20px;
  }
  .hero h1 {
    font-size: 28px !important;
  }
}
</style>
