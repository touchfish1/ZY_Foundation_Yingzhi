<template>
  <div ref="elRef" class="anim-ripple">
    <slot />
    <div class="ripple-container">
      <span
        v-for="r in ripples"
        :key="r.id"
        class="ripple"
        :style="{
          left: `${r.x}px`,
          top: `${r.y}px`,
          width: `${r.size}px`,
          height: `${r.size}px`,
          background: color,
          animationDuration: `${duration}ms`
        }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  color?: string
  duration?: number
}>(), {
  color: 'rgba(168,50,43,0.3)',
  duration: 600
})

const elRef = ref<HTMLElement | null>(null)
const { ripples } = useRipple(elRef, { color: props.color, duration: props.duration })
</script>

<style scoped>
.anim-ripple {
  position: relative;
  overflow: hidden;
}

.ripple-container {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 1;
  border-radius: inherit;
}

.ripple {
  position: absolute;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: ripple-effect ease-out forwards;
}

@keyframes ripple-effect {
  from { transform: translate(-50%, -50%) scale(0); opacity: 1; }
  to { transform: translate(-50%, -50%) scale(3); opacity: 0; }
}
</style>
