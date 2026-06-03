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
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.hero {
  width: min(1120px, calc(100% - 40px));
  margin: 68px auto 80px;
  text-align: center;
}

.eyebrow {
  color: #67e8f9;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

h1 {
  max-width: 900px;
  margin: 16px auto;
  font-size: clamp(44px, 8vw, 92px);
  line-height: 0.95;
  letter-spacing: -0.06em;
}

.subtitle {
  max-width: 680px;
  margin: 0 auto;
  color: rgba(226, 232, 240, 0.74);
  font-size: 20px;
}

.actions {
  margin-top: 34px;
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}

a {
  padding: 13px 20px;
  border-radius: 999px;
  text-decoration: none;
}

.primary {
  color: #07111f;
  background: #67e8f9;
  font-weight: 800;
}

.secondary {
  border: 1px solid rgba(255, 255, 255, 0.16);
}
</style>
