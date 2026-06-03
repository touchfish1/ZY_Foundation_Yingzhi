<template>
  <section class="cta">
    <div class="cta-inner">
      <h2>{{ props.title || '准备开始了吗？' }}</h2>
      <p v-if="props.subtitle" class="cta-sub">{{ props.subtitle }}</p>
      <a class="cta-btn" :href="String(props.buttonUrl || '#')">{{ props.buttonText || '立即开始' }}</a>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.cta {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto 96px;
  padding: 64px 40px;
  border-radius: 0.75rem;
  text-align: center;
  background: linear-gradient(135deg, #0ea5e9 0%, #6366f1 100%);
  color: #fff;
}

.cta-inner {
  max-width: 640px;
  margin: 0 auto;
}

h2 {
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  letter-spacing: -0.03em;
  margin: 0 0 12px;
}

.cta-sub {
  font-size: 16px;
  opacity: 0.85;
  margin: 0 0 28px;
  line-height: 1.6;
}

.cta-btn {
  display: inline-block;
  padding: 12px 32px;
  border-radius: 0.5rem;
  background: #fff;
  color: #0a0a0a;
  font-weight: 600;
  font-size: 15px;
  text-decoration: none;
  transition: all 0.2s;
}

.cta-btn:hover {
  background: #f5f5f5;
  transform: translateY(-1px);
}
</style>
