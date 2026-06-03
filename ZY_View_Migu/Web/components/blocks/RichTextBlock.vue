<template>
  <section class="rich" v-html="String(props.content || '')" />
</template>

<script setup lang="ts">
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.rich {
  width: min(768px, calc(100% - 40px));
  margin: 0 auto 96px;
  color: #0a0a0a;
  line-height: 1.8;
  font-size: 16px;
}

.rich :deep(p) {
  margin: 0 0 16px;
}

.rich :deep(h2),
.rich :deep(h3),
.rich :deep(h4) {
  margin: 28px 0 12px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.rich :deep(a) {
  color: #0ea5e9;
  text-decoration: underline;
}
</style>
