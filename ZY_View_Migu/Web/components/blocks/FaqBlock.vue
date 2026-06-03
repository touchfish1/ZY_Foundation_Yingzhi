<template>
  <section id="faq" class="block">
    <h2>{{ props.title || '常见问题' }}</h2>
  </section>
</template>

<script setup lang="ts">
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.block {
  width: min(920px, calc(100% - 40px));
  margin: 0 auto 80px;
}
</style>
