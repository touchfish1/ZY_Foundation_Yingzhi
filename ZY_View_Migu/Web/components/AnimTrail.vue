<template>
  <div class="trail-container">
    <span v-for="(dot, i) in trail" :key="i" :style="dotStyle(dot)" />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  count?: number
  size?: number
  color?: string
}>(), {
  count: 8,
  size: 5,
  color: 'rgba(168,50,43,0.6)',
})

const { trail } = useMouseTrail({ count: props.count })

function dotStyle(dot: { x: number; y: number; opacity: number; scale: number }) {
  const s = props.size * dot.scale
  return {
    left: dot.x + 'px',
    top: dot.y + 'px',
    width: s + 'px',
    height: s + 'px',
    opacity: dot.opacity,
    background: props.color,
    transform: 'translate(-50%, -50%)',
  }
}
</script>

<style scoped>
.trail-container {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9997;
}

.trail-container span {
  position: fixed;
  border-radius: 50%;
  transition: all 0.08s ease-out;
}
</style>
