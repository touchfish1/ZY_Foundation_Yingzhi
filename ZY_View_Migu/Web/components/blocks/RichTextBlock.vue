<template>
  <section class="rich" v-html="String(props.content || '')" />
</template>

<script setup lang="ts">
defineProps<{ props: Record<string, unknown> }>()
</script>

<style scoped>
.rich {
  width: min(900px, calc(100% - 40px));
  margin: 0 auto 80px;
  color: rgba(226, 232, 240, 0.82);
}
</style>
