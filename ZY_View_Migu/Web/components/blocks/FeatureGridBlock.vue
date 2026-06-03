<template>
  <section class="features">
    <h2>{{ props.title || '核心能力' }}</h2>
    <p v-if="props.subtitle" class="features-sub">{{ props.subtitle }}</p>
    <div class="grid">
      <article v-for="(item, i) in items" :key="i" class="card">
        <div v-if="item.icon" class="icon">{{ item.icon }}</div>
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
const props = defineProps<{ props: Record<string, unknown> }>()
const items = computed(() => {
  const raw = props.props.items
  return Array.isArray(raw) ? raw : []
})
</script>

<style scoped>
.features {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto 96px;
}

h2 {
  text-align: center;
  font-size: clamp(24px, 4vw, 36px);
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0a0a0a;
  margin: 0 0 12px;
}

.features-sub {
  text-align: center;
  color: #606060;
  font-size: 16px;
  margin: 0 0 40px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

@media (min-width: 768px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.card {
  padding: 28px;
  border-radius: 0.75rem;
  background: #fff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 3px 0 rgba(0,0,0,0.10);
  transition: all 0.25s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.icon {
  font-size: 28px;
  margin-bottom: 12px;
}

h3 {
  font-size: 18px;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 8px;
}

.card p {
  color: #606060;
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
}
</style>
