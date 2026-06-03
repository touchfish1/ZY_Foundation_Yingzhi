<template>
  <section id="faq" class="faq">
    <h2>常见问题</h2>
    <div class="list">
      <details v-for="(item, i) in items" :key="i" class="item">
        <summary>{{ item.question }}</summary>
        <p>{{ item.answer }}</p>
      </details>
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
.faq {
  width: min(768px, calc(100% - 40px));
  margin: 0 auto 96px;
}

h2 {
  text-align: center;
  font-size: clamp(24px, 4vw, 36px);
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0a0a0a;
  margin: 0 0 40px;
}

.list {
  border-top: 1px solid #e8e8e8;
}

.item {
  border-bottom: 1px solid #e8e8e8;
  padding: 20px 0;
}

summary {
  font-weight: 600;
  font-size: 16px;
  color: #0a0a0a;
  cursor: pointer;
  list-style: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

summary::after {
  content: '+';
  font-size: 20px;
  color: #606060;
  transition: transform 0.2s;
}

details[open] summary::after {
  content: '−';
}

.item p {
  margin: 16px 0 0;
  color: #606060;
  line-height: 1.7;
  font-size: 15px;
}
</style>
