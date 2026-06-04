<template>
  <div
    v-if="isVisible"
    class="cursor-glow"
    :style="glowStyle"
  />
</template>

<script setup lang="ts">
const { x, y, isVisible } = useMouseGlow()

const glowStyle = computed(() => {
  const size = 500
  return {
    background: `radial-gradient(${size}px at ${x.value}px ${y.value}px, rgba(255,220,180,0.25), transparent 70%)`
  }
})
</script>

<style scoped>
.cursor-glow {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9998;
  transition: background 0.06s ease-out;
}
</style>
