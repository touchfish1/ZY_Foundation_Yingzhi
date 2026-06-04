<template>
  <div ref="elRef" :style="tiltStyle" style="position:relative;overflow:hidden">
    <div v-if="glare" :style="glareStyle" style="position:absolute;inset:0;pointer-events:none;z-index:1" />
    <slot />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  maxTilt?: number
  perspective?: number
  scale?: number
  glare?: boolean
}>(), {
  maxTilt: 8,
  perspective: 1000,
  scale: 1.02,
  glare: true
})

const elRef = ref<HTMLElement | null>(null)
const { style: tiltStyle, glareStyle } = useParallaxTilt(elRef, {
  maxTilt: props.maxTilt,
  perspective: props.perspective,
  scale: props.scale,
  glare: props.glare
})
</script>
