<template>
  <section class="features">
    <h2>{{ props.title || '核心能力' }}</h2>
  </section>
</template>

<script setup lang="ts">
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.features {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto 80px;
}
</style>
