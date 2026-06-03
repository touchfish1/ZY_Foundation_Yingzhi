<template>
  <section class="cta">
    <h2>{{ props.title || '准备开始了吗？' }}</h2>
    <a :href="String(props.buttonUrl || '#')">{{ props.buttonText || '立即开始' }}</a>
  </section>
</template>

<script setup lang="ts">
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.cta {
  width: min(920px, calc(100% - 40px));
  margin: 0 auto 96px;
  padding: 40px;
  border-radius: 32px;
  text-align: center;
  background: #67e8f9;
  color: #07111f;
}

a {
  color: inherit;
  font-weight: 900;
}
</style>
